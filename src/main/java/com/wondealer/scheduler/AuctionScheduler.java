package com.wondealer.scheduler;

import com.wondealer.entity.Auction;
import com.wondealer.entity.Bid;
import com.wondealer.entity.ChatRoom;
import com.wondealer.entity.Member;
import com.wondealer.entity.Wallet;
import com.wondealer.entity.WalletTx;
import com.wondealer.entity.WalletTxType;
import com.wondealer.repository.AuctionRepository;
import com.wondealer.repository.BidRepository;
import com.wondealer.repository.ChatRoomRepository;
import com.wondealer.repository.WalletRepository;
import com.wondealer.repository.WalletTxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private static final String BID_STATUS_VALID = "VALID";
    private static final String BID_STATUS_OUTBID = "OUTBID";

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final WalletRepository walletRepository;
    private final WalletTxRepository walletTxRepository;

    /**
     * 1분마다 종료된 경매를 처리한다.
     * endTime이 지난 ONGOING 경매를 찾아:
     *   - 입찰자 있음 → 최고 입찰자 낙찰 처리 + 채팅방 생성
     *   - 입찰자 없음 → 경매 취소 처리
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    @Transactional
    public void processExpiredAuctions() {
        List<Auction> expiredAuctions = auctionRepository
                .findExpiredOngoingAuctions(LocalDateTime.now());

        if (expiredAuctions.isEmpty()) return;

        log.info("[경매 배치] 종료된 경매 {}건 처리 시작", expiredAuctions.size());

        for (Auction auction : expiredAuctions) {
            try {
                processAuction(auction);
            } catch (Exception e) {
                log.error("[경매 배치] 경매 ID {} 처리 중 오류 발생: {}",
                        auction.getId(), e.getMessage());
            }
        }

        log.info("[경매 배치] 처리 완료");
    }

    private void processAuction(Auction auction) {
        Optional<Bid> topBid = bidRepository
                .findTopByAuctionIdAndStatusOrderByBidPriceDesc(
                        auction.getId(), BID_STATUS_VALID);

        if (topBid.isPresent()) {
            // 입찰자 있음 → 낙찰 처리
            settleAuction(auction, topBid.get());
        } else {
            // 입찰자 없음 → 경매 취소
            cancelAuction(auction);
        }
    }

    /**
     * 낙찰 처리:
     * 최고 입찰자를 낙찰자로 설정하고 채팅방을 생성한다.
     */
    private void settleAuction(Auction auction, Bid winningBid) {
        Member winner = winningBid.getBidder();

        auction.endWithWinner(winner);
        auction.getItem().completeItem();

        // 낙찰자 + 판매자 채팅방 자동 생성
        boolean exists = chatRoomRepository
                .findByItemIdAndBuyerId(auction.getItem().getId(), winner.getId())
                .isPresent();

        if (!exists) {
            chatRoomRepository.save(ChatRoom.builder()
                    .item(auction.getItem())
                    .buyer(winner)
                    .seller(auction.getItem().getSeller())
                    .build());
        }

        log.info("[경매 배치] 경매 ID {} 낙찰 완료 → 낙찰자: {}",
                auction.getId(), winner.getNickname());
    }

    /**
     * 경매 취소 처리:
     * 입찰자가 없으면 경매를 취소하고 상품을 삭제 상태로 변경한다.
     * 혹시 VALID 입찰이 남아있으면 전부 환불한다.
     */
    private void cancelAuction(Auction auction) {
        auction.cancelAuction();
        auction.getItem().deleteBySeller();

        log.info("[경매 배치] 경매 ID {} 취소 처리 (입찰자 없음)", auction.getId());
    }
}
