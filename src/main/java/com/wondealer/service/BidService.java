package com.wondealer.service;

import com.wondealer.dto.request.AuctionBidReqDto;
import com.wondealer.dto.response.AuctionBidResDto;
import com.wondealer.dto.websocket.AuctionBidBroadcastDto;
import com.wondealer.entity.Auction;
import com.wondealer.entity.Bid;
import com.wondealer.entity.ChatRoom;
import com.wondealer.entity.Member;
import com.wondealer.entity.Wallet;
import com.wondealer.entity.WalletTx;
import com.wondealer.entity.WalletTxType;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.AuctionRepository;
import com.wondealer.repository.BidRepository;
import com.wondealer.repository.ChatRoomRepository;
import com.wondealer.repository.MemberRepository;
import com.wondealer.repository.WalletRepository;
import com.wondealer.repository.WalletTxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

    private static final String AUCTION_STATUS_ONGOING = "ONGOING";
    private static final String BID_STATUS_VALID = "VALID";
    private static final String BID_STATUS_OUTBID = "OUTBID";
    private static final String BID_STATUS_WON = "WON";

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final WalletTxRepository walletTxRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AuctionBidResDto placeBid(Long auctionId, Long bidderId, AuctionBidReqDto dto) {
        Auction auction = auctionRepository.findByIdWithPessimisticLock(auctionId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다."));

        validateAuctionCanBid(auction, bidderId, dto.getBidPrice());

        Member bidder = memberRepository.findById(bidderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        Wallet bidderWallet = walletRepository.findByMemberId(bidderId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "WonPay 지갑을 찾을 수 없습니다."));

        Optional<Bid> previousBid = bidRepository.findTopByAuctionIdAndStatusOrderByBidPriceDesc(
                auctionId, BID_STATUS_VALID);

        if (previousBid.isPresent() && previousBid.get().getBidder().getId().equals(bidderId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "현재 최고 입찰자는 재입찰할 수 없습니다.");
        }

        if (bidderWallet.getBalance() < dto.getBidPrice()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "WonPay 잔액이 부족합니다.");
        }

        // 입찰자 잔액 즉시 차감
        bidderWallet.withdraw(dto.getBidPrice());
        saveWalletTx(bidderWallet, WalletTxType.USE, dto.getBidPrice(), "경매 입찰 금액");

        // 이전 최고 입찰자 즉시 환불
        previousBid.filter(bid -> !bid.getBidder().getId().equals(bidderId))
                .ifPresent(bid -> refundPreviousBid(bid, "경매 입찰 환불"));

        // 경매 현재가, 입찰 수 업데이트
        auction.updateBid(dto.getBidPrice());

        Bid bid = bidRepository.save(Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .bidPrice(dto.getBidPrice())
                .build());

        // 즉시 낙찰 처리
        if (auction.getInstantBuyPrice() != null
                && dto.getBidPrice() >= auction.getInstantBuyPrice()) {
            auction.endWithWinner(bidder);
            bid.changeStatus(BID_STATUS_WON);

            // Item 상태 COMPLETED로 변경
            auction.getItem().completeItem();

            // 낙찰자 + 판매자 채팅방 자동 생성
            createChatRoomForWinner(auction, bidder);
        }

        broadcastBid(auction, bidder);
        return AuctionBidResDto.from(auction, bid);
    }

    /**
     * 즉시 낙찰 시 낙찰자와 판매자의 채팅방을 자동 생성한다.
     * 게임 내 거래 일정/장소 조율을 위해 필요하다.
     */
    private void createChatRoomForWinner(Auction auction, Member winner) {
        Member seller = auction.getItem().getSeller();

        // 이미 채팅방이 있으면 생성하지 않음
        boolean exists = chatRoomRepository
                .findByItemIdAndBuyerId(auction.getItem().getId(), winner.getId())
                .isPresent();

        if (!exists) {
            chatRoomRepository.save(ChatRoom.builder()
                    .item(auction.getItem())
                    .buyer(winner)
                    .seller(seller)
                    .build());
        }
    }

    private void validateAuctionCanBid(Auction auction, Long bidderId, Long bidPrice) {
        if (!AUCTION_STATUS_ONGOING.equals(auction.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "진행 중인 경매에만 입찰할 수 있습니다.");
        }

        if (!auction.getEndTime().isAfter(LocalDateTime.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "종료된 경매입니다.");
        }

        if (auction.getItem().getSeller().getId().equals(bidderId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "본인 경매에 입찰할 수 없습니다.");
        }

        if (bidPrice <= auction.getCurrentPrice()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "현재가보다 높은 금액으로 입찰해야 합니다.");
        }
    }

    private void refundPreviousBid(Bid previousBid, String description) {
        Wallet previousWallet = walletRepository.findByMemberId(
                        previousBid.getBidder().getId())
                .orElseThrow(() -> new CustomException(
                        HttpStatus.BAD_REQUEST, "이전 입찰자의 지갑을 찾을 수 없습니다."));

        previousWallet.deposit(previousBid.getBidPrice());
        previousBid.changeStatus(BID_STATUS_OUTBID);
        saveWalletTx(previousWallet, WalletTxType.REFUND,
                previousBid.getBidPrice(), description);
    }

    private void saveWalletTx(Wallet wallet, WalletTxType type,
                              Long amount, String description) {
        walletTxRepository.save(WalletTx.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .fee(0L)
                .tradeId(null)
                .description(description)
                .build());
    }

    private void broadcastBid(Auction auction, Member bidder) {
        messagingTemplate.convertAndSend(
                "/topic/auction/" + auction.getId(),
                AuctionBidBroadcastDto.builder()
                        .auctionId(auction.getId())
                        .currentPrice(auction.getCurrentPrice())
                        .bidCount(auction.getBidCount())
                        .bidderId(bidder.getId())
                        .bidderNickname(bidder.getNickname())
                        .status(auction.getStatus())
                        .winnerId(auction.getWinner() == null ? null : auction.getWinner().getId())
                        .build()
        );
    }
}