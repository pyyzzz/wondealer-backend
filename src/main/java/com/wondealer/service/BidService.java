package com.wondealer.service;

import com.wondealer.dto.request.AuctionBidReqDto;
import com.wondealer.dto.response.AuctionBidResDto;
import com.wondealer.dto.websocket.AuctionBidBroadcastDto;
import com.wondealer.entity.Auction;
import com.wondealer.entity.Bid;
import com.wondealer.entity.Member;
import com.wondealer.entity.Wallet;
import com.wondealer.entity.WalletTx;
import com.wondealer.entity.WalletTxType;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.AuctionRepository;
import com.wondealer.repository.BidRepository;
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
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public AuctionBidResDto placeBid(Long auctionId, Long bidderId, AuctionBidReqDto dto) {
        Auction auction = auctionRepository.findByIdWithPessimisticLock(auctionId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Auction not found."));

        validateAuctionCanBid(auction, bidderId, dto.getBidPrice());

        Member bidder = memberRepository.findById(bidderId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Member not found."));
        Wallet bidderWallet = walletRepository.findByMemberId(bidderId)
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "WonPay wallet not found."));

        Optional<Bid> previousBid = bidRepository.findTopByAuctionIdAndStatusOrderByBidPriceDesc(
                auctionId, BID_STATUS_VALID);

        if (previousBid.isPresent() && previousBid.get().getBidder().getId().equals(bidderId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Current highest bidder cannot bid again.");
        }

        if (bidderWallet.getBalance() < dto.getBidPrice()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "WonPay balance is not enough.");
        }

        bidderWallet.withdraw(dto.getBidPrice());
        saveWalletTx(bidderWallet, WalletTxType.USE, dto.getBidPrice(), "Auction bid payment.");

        previousBid.filter(bid -> !bid.getBidder().getId().equals(bidderId))
                .ifPresent(bid -> refundPreviousBid(bid, "Refund outbid auction payment."));

        auction.updateBid(dto.getBidPrice());

        Bid bid = bidRepository.save(Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .bidPrice(dto.getBidPrice())
                .build());

        if (auction.getInstantBuyPrice() != null && dto.getBidPrice() >= auction.getInstantBuyPrice()) {
            auction.endWithWinner(bidder);
            bid.changeStatus(BID_STATUS_WON);
        }

        broadcastBid(auction, bidder);
        return AuctionBidResDto.from(auction, bid);
    }

    private void validateAuctionCanBid(Auction auction, Long bidderId, Long bidPrice) {
        if (!AUCTION_STATUS_ONGOING.equals(auction.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Only ongoing auctions can receive bids.");
        }

        if (!auction.getEndTime().isAfter(LocalDateTime.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Auction has ended.");
        }

        if (auction.getItem().getSeller().getId().equals(bidderId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Seller cannot bid on own auction.");
        }

        if (bidPrice <= auction.getCurrentPrice()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Bid price must be higher than current price.");
        }
    }

    private void refundPreviousBid(Bid previousBid, String description) {
        Wallet previousWallet = walletRepository.findByMemberId(previousBid.getBidder().getId())
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "Previous bidder wallet not found."));

        previousWallet.deposit(previousBid.getBidPrice());
        previousBid.changeStatus(BID_STATUS_OUTBID);
        saveWalletTx(previousWallet, WalletTxType.REFUND, previousBid.getBidPrice(), description);
    }

    private void saveWalletTx(Wallet wallet, WalletTxType type, Long amount, String description) {
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
                        .build()
        );
    }
}