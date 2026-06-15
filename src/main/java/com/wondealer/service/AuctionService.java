package com.wondealer.service;

import com.wondealer.dto.response.AuctionDetailResDto;
import com.wondealer.entity.Auction;
import com.wondealer.entity.ItemStatus;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

    private final AuctionRepository auctionRepository;

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
}
