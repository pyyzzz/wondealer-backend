package com.wondealer.service;

import com.wondealer.dto.response.AuctionDetailResDto;
import com.wondealer.dto.response.AuctionListResDto;
import com.wondealer.dto.response.PageResDto;
import com.wondealer.entity.Auction;
import com.wondealer.entity.ItemStatus;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

    private static final String ONGOING = "ONGOING";
    private static final int NO_BID_COUNT = 0;

    private final AuctionRepository auctionRepository;

    /**
     * 진행 중인 경매 목록을 조회한다.
     * gameId가 없으면 전체 진행 중 경매를, 있으면 해당 게임의 진행 중 경매만 조회한다.
     */
    public PageResDto<AuctionListResDto> getAuctions(Long gameId, Pageable pageable) {
        Page<Auction> auctions = gameId == null
                ? auctionRepository.findByStatusAndItem_StatusAndItem_IsDeletedByAdminFalse(
                        ONGOING, ItemStatus.SELLING, pageable)
                : auctionRepository.findByStatusAndItem_StatusAndItem_IsDeletedByAdminFalseAndItem_GameCategory_Game_Id(
                        ONGOING, ItemStatus.SELLING, gameId, pageable);

        return PageResDto.from(auctions.map(AuctionListResDto::from));
    }

    /**
     * 경매 ID로 경매 상세 정보를 조회한다.
     * 삭제된 상품과 연결된 경매는 사용자에게 노출하지 않는다.
     */
    public AuctionDetailResDto getAuctionDetail(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다."));

        if (auction.getItem().isDeletedByAdmin() || auction.getItem().getStatus() == ItemStatus.DELETED) {
            throw new CustomException(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다.");
        }

        return AuctionDetailResDto.from(auction);
    }

    /**
     * 판매자 본인이 입찰자가 없는 진행 중 경매를 취소한다.
     * 경매만 CANCELED로 바꾸면 연결된 Item이 목록에 남을 수 있으므로 Item도 DELETED 상태로 함께 바꾼다.
     */
    @Transactional
    public void cancelAuction(Long memberId, Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다."));

        if (auction.getItem().isDeletedByAdmin() || auction.getItem().getStatus() == ItemStatus.DELETED) {
            throw new CustomException(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다.");
        }

        if (!auction.getItem().getSeller().getId().equals(memberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "본인 경매만 취소할 수 있습니다.");
        }

        if (!ONGOING.equals(auction.getStatus())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "진행 중인 경매만 취소할 수 있습니다.");
        }

        if (auction.getBidCount() > NO_BID_COUNT) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "입찰자가 있는 경매는 취소할 수 없습니다.");
        }

        auction.cancelAuction();
        auction.getItem().deleteBySeller();
    }
}
