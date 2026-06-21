package com.wondealer.service;

import com.wondealer.dto.request.TradeCreateReqDto;
import com.wondealer.dto.response.TradeResDto;
import com.wondealer.entity.Auction;
import com.wondealer.entity.ChatRoom;
import com.wondealer.entity.Item;
import com.wondealer.entity.ItemStatus;
import com.wondealer.entity.Member;
import com.wondealer.entity.Payment;
import com.wondealer.entity.Trade;
import com.wondealer.entity.TradeStatus;
import com.wondealer.entity.TradeType;
import com.wondealer.entity.Wallet;
import com.wondealer.entity.WalletTx;
import com.wondealer.entity.WalletTxType;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.AuctionRepository;
import com.wondealer.repository.ChatRoomRepository;
import com.wondealer.repository.ItemRepository;
import com.wondealer.repository.MemberRepository;
import com.wondealer.repository.PaymentRepository;
import com.wondealer.repository.TradeRepository;
import com.wondealer.repository.WalletRepository;
import com.wondealer.repository.WalletTxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeService {

    private static final String PAYMENT_WONPAY = "WONPAY";
    private static final String PAYMENT_PORTONE = "PORTONE";
    private static final String PORTONE_PAID_STATUS = "PAID";
    private static final String AUCTION_STATUS_ENDED = "ENDED";

    private final TradeRepository tradeRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final WalletTxRepository walletTxRepository;
    private final PaymentRepository paymentRepository;
    private final AuctionRepository auctionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PortOneService portOneService;

    @Value("${trade.rate:0.05}")
    private double tradeRate;

    @Transactional
    public TradeResDto createDirectTrade(Long buyerId, TradeCreateReqDto dto) {
        Member buyer = findMember(buyerId);
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        validateDirectTradeRequest(buyerId, item);
        if (tradeRepository.findByItemId(item.getId()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 거래가 진행 중인 상품입니다.");
        }

        Trade trade = tradeRepository.save(Trade.builder()
                .item(item)
                .seller(item.getSeller())
                .buyer(buyer)
                .tradePrice(item.getPrice())
                .tradeType(TradeType.DIRECT)
                .build());

        String paymentMethod = dto.getPaymentMethod().toUpperCase();
        if (PAYMENT_WONPAY.equals(paymentMethod)) {
            payDirectTradeWithWonPay(trade, buyerId);
        } else if (PAYMENT_PORTONE.equals(paymentMethod)) {
            payDirectTradeWithPortOne(trade, dto.getPaymentId());
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "지원하지 않는 결제 방식입니다.");
        }

        item.reserveItem();

        // 결제 완료 후 채팅방에 Trade 연결 (새로고침 시 결제 상태 유지)
        chatRoomRepository.findByItemIdAndBuyerId(item.getId(), buyerId)
                .ifPresent(chatRoom -> chatRoom.assignTrade(trade));

        return TradeResDto.from(trade);
    }

    @Transactional
    public TradeResDto confirmTrade(Long buyerId, Long tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "거래를 찾을 수 없습니다."));

        if (!trade.getBuyer().getId().equals(buyerId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "구매자만 거래를 확정할 수 있습니다.");
        }

        settleCompletedTrade(trade);
        return TradeResDto.from(trade);
    }

    /**
     * 경매 낙찰 정산 처리
     * 낙찰자(winner)만 호출 가능
     */
    @Transactional
    public TradeResDto settleAuction(Long memberId, Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다."));

        if (!AUCTION_STATUS_ENDED.equals(auction.getStatus()) || auction.getWinner() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "낙찰 완료된 경매만 정산할 수 있습니다.");
        }

        // 낙찰자 본인만 정산 가능
        if (!auction.getWinner().getId().equals(memberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "낙찰자만 정산을 요청할 수 있습니다.");
        }

        if (tradeRepository.findByItemId(auction.getItem().getId()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 정산된 경매입니다.");
        }

        Trade trade = tradeRepository.save(Trade.builder()
                .item(auction.getItem())
                .seller(auction.getItem().getSeller())
                .buyer(auction.getWinner())
                .tradePrice(auction.getCurrentPrice())
                .tradeType(TradeType.AUCTION)
                .build());
        trade.completePayment();

        settleCompletedTrade(trade);
        return TradeResDto.from(trade);
    }

    private void validateDirectTradeRequest(Long buyerId, Item item) {
        if (item.getTradeType() != TradeType.DIRECT) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "일반 판매 상품만 직접 거래할 수 있습니다.");
        }

        if (item.getStatus() != ItemStatus.SELLING || item.isDeletedByAdmin()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "거래할 수 없는 상품입니다.");
        }

        if (item.getSeller().getId().equals(buyerId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "본인 상품은 구매할 수 없습니다.");
        }
    }

    private void payDirectTradeWithWonPay(Trade trade, Long buyerId) {
        int updatedRows = walletRepository.deductBalance(buyerId, trade.getTradePrice());
        if (updatedRows == 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "WonPay 잔액이 부족합니다.");
        }

        Wallet buyerWallet = walletRepository.findByMemberId(buyerId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "WonPay 지갑이 없습니다."));

        saveWalletTx(buyerWallet, WalletTxType.USE, trade.getTradePrice(),
                trade.getId(), "직거래 WonPay 결제", 0L);
        trade.completePayment();

        Payment payment = paymentRepository.save(Payment.builder()
                .trade(trade)
                .member(trade.getBuyer())
                .amount(trade.getTradePrice())
                .pgTransactionId("WONPAY")
                .build());
        payment.completePayment("WONPAY");
    }

    private void payDirectTradeWithPortOne(Trade trade, String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "포트원 paymentId가 필요합니다.");
        }

        PortOneService.PortOnePayment portOnePayment = portOneService.getPayment(paymentId);
        if (!PORTONE_PAID_STATUS.equals(portOnePayment.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "포트원 결제가 완료되지 않았습니다.");
        }

        if (!trade.getTradePrice().equals(portOnePayment.getTotalAmount())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "결제 금액이 거래 금액과 일치하지 않습니다.");
        }

        trade.completePayment();
        Payment savedPayment = paymentRepository.save(Payment.builder()
                .trade(trade)
                .member(trade.getBuyer())
                .amount(trade.getTradePrice())
                .pgTransactionId(paymentId)
                .build());
        savedPayment.completePayment(paymentId);
    }

    private void settleCompletedTrade(Trade trade) {
        if (trade.getStatus() == TradeStatus.COMPLETED) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 완료된 거래입니다.");
        }
        if (trade.getStatus() != TradeStatus.PAID) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "결제 완료 상태의 거래만 확정할 수 있습니다.");
        }

        trade.completeTrade();
        trade.getItem().completeItem();

        long fee = calculateFee(trade.getTradePrice());
        long settlementAmount = trade.getTradePrice() - fee;
        Wallet sellerWallet = walletRepository.findByMemberId(trade.getSeller().getId())
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "판매자 지갑이 없습니다."));

        sellerWallet.deposit(settlementAmount);
        saveWalletTx(sellerWallet, WalletTxType.SETTLEMENT, settlementAmount,
                trade.getId(), "거래 판매자 정산", fee);
        expireChatRooms(trade);
    }

    private long calculateFee(Long tradePrice) {
        return (long) Math.floor(tradePrice * tradeRate);
    }

    private void expireChatRooms(Trade trade) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByItemId(trade.getItem().getId());
        for (ChatRoom chatRoom : chatRooms) {
            if (chatRoom.getBuyer().getId().equals(trade.getBuyer().getId())) {
                chatRoom.connectTradeAndSetExpiry(trade);
            } else {
                chatRoom.deactivate();
            }
        }
    }

    private void saveWalletTx(Wallet wallet, WalletTxType type, Long amount,
                              Long tradeId, String description, Long fee) {
        walletTxRepository.save(WalletTx.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .fee(fee)
                .tradeId(tradeId)
                .description(description)
                .build());
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
    }
}