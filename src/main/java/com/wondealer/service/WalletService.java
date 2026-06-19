package com.wondealer.service;

import com.wondealer.dto.request.WalletChargeCompleteReqDto;
import com.wondealer.dto.request.WalletChargeReadyReqDto;
import com.wondealer.dto.request.WalletWithdrawReqDto;
import com.wondealer.dto.response.WalletChargeCompleteResDto;
import com.wondealer.dto.response.WalletChargeReadyResDto;
import com.wondealer.dto.response.WalletResDto;
import com.wondealer.dto.response.WalletWithdrawResDto;
import com.wondealer.entity.Member;
import com.wondealer.entity.Wallet;
import com.wondealer.entity.WalletCharge;
import com.wondealer.entity.WalletTx;
import com.wondealer.entity.WalletTxType;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.MemberRepository;
import com.wondealer.repository.WalletChargeRepository;
import com.wondealer.repository.WalletRepository;
import com.wondealer.repository.WalletTxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {

    private static final String ORDER_NAME = "WonPay 충전";
    private static final String CURRENCY_KRW = "CURRENCY_KRW";
    private static final String PORTONE_PAID_STATUS = "PAID";
    private static final String WITHDRAW_PENDING_STATUS = "PENDING";

    @Value("${withdraw.rate:0.01}")
    private double withdrawRate;

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final WalletTxRepository walletTxRepository;
    private final WalletChargeRepository walletChargeRepository;
    private final PortOneService portOneService;

    /**
     * WonPay 잔액 및 등록 계좌 조회
     */
    public WalletResDto getWallet(Long memberId) {
        Member member = findUsableMember(memberId);
        Wallet wallet = getOrCreateWallet(member);
        return WalletResDto.of(wallet, member);
    }

    @Transactional
    public WalletChargeReadyResDto prepareCharge(Long memberId, WalletChargeReadyReqDto dto) {
        Member member = findUsableMember(memberId);
        getOrCreateWallet(member);

        String paymentId = "wonpay-" + memberId + "-" + UUID.randomUUID();

        WalletCharge charge = WalletCharge.builder()
                .member(member)
                .paymentId(paymentId)
                .amount(dto.getAmount())
                .build();
        walletChargeRepository.save(charge);

        return WalletChargeReadyResDto.builder()
                .paymentId(paymentId)
                .amount(charge.getAmount())
                .orderName(ORDER_NAME)
                .currency(CURRENCY_KRW)
                .build();
    }

    @Transactional
    public WalletChargeCompleteResDto completeCharge(Long memberId, WalletChargeCompleteReqDto dto) {
        WalletCharge charge = walletChargeRepository.findByPaymentId(dto.getPaymentId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "충전 요청을 찾을 수 없습니다."));

        if (!charge.getMember().getId().equals(memberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "본인의 충전 요청만 완료할 수 있습니다.");
        }

        Member member = findUsableMember(memberId);
        Wallet wallet = getOrCreateWallet(member);

        if (charge.isCompleted()) {
            return WalletChargeCompleteResDto.builder()
                    .balance(wallet.getBalance())
                    .chargedAmount(charge.getAmount())
                    .build();
        }

        PortOneService.PortOnePayment payment = portOneService.getPayment(charge.getPaymentId());
        validatePaidPayment(charge, payment);

        wallet.deposit(charge.getAmount());
        charge.complete();
        walletTxRepository.save(WalletTx.builder()
                .wallet(wallet)
                .type(WalletTxType.CHARGE)
                .amount(charge.getAmount())
                .fee(0L)
                .tradeId(null)
                .description(ORDER_NAME)
                .build());

        return WalletChargeCompleteResDto.builder()
                .balance(wallet.getBalance())
                .chargedAmount(charge.getAmount())
                .build();
    }

    /**
     * WonPay 출금 신청
     * Member에 등록된 계좌로 자동 출금 (방식 B 확정)
     * 수수료: withdraw.rate=0.01 (1%)
     */
    @Transactional
    public WalletWithdrawResDto withdraw(Long memberId, WalletWithdrawReqDto dto) {
        Member member = findUsableMember(memberId);

        if (member.getBankName() == null || member.getAccountNumber() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "출금 계좌를 먼저 등록해주세요. (마이페이지 → 계좌 등록)");
        }

        Wallet wallet = walletRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "WonPay 지갑이 없습니다."));

        long fee = (long) Math.floor(dto.getAmount() * withdrawRate);
        long actualAmount = dto.getAmount() - fee;

        if (wallet.getBalance() < dto.getAmount()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "잔액이 부족합니다.");
        }

        wallet.withdraw(dto.getAmount());

        WalletTx walletTx = walletTxRepository.save(WalletTx.builder()
                .wallet(wallet)
                .type(WalletTxType.WITHDRAW)
                .amount(dto.getAmount())
                .fee(fee)
                .tradeId(null)
                .description("WonPay 출금: " + member.getBankName()
                        + " " + maskAccountNumber(member.getAccountNumber()))
                .build());

        return WalletWithdrawResDto.builder()
                .withdrawId(walletTx.getId())
                .amount(dto.getAmount())
                .fee(fee)
                .actualAmount(actualAmount)
                .bankName(member.getBankName())
                .accountNumber(maskAccountNumber(member.getAccountNumber()))
                .status(WITHDRAW_PENDING_STATUS)
                .build();
    }

    private Member findUsableMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        if (member.isBanned()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "정지된 회원은 WonPay를 사용할 수 없습니다.");
        }

        return member;
    }

    private Wallet getOrCreateWallet(Member member) {
        return walletRepository.findByMemberId(member.getId())
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .member(member)
                        .build()));
    }

    private void validatePaidPayment(WalletCharge charge, PortOneService.PortOnePayment payment) {
        if (!PORTONE_PAID_STATUS.equals(payment.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "결제가 완료되지 않았습니다.");
        }

        if (!charge.getAmount().equals(payment.getTotalAmount())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "결제 금액이 충전 요청 금액과 일치하지 않습니다.");
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
