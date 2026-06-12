package com.wondealer.service;

import com.wondealer.dto.request.ItemReqDto;
import com.wondealer.dto.response.ItemDetailResDto;
import com.wondealer.dto.response.ItemResDto;
import com.wondealer.entity.*;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.GameCategoryRepository;
import com.wondealer.repository.GameServerRepository;
import com.wondealer.repository.ItemRepository;
import com.wondealer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final GameCategoryRepository gameCategoryRepository;
    private final GameServerRepository gameServerRepository;

    public ItemResDto createDirectItem(Long memberId, ItemReqDto dto) {
        Member seller = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "로그인 후 상품 등록이 가능합니다."));

        if (!seller.isEmailVerified()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "이메일 인증 후 상품을 등록할 수 있습니다.");
        }

        if (seller.isBanned()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "정지된 회원은 상품을 등록할 수 없습니다.");
        }

        GameCategory category = gameCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게임을 선택해주세요."));

        GameServer server = null;
        if (dto.getServerId() != null) {
            server = gameServerRepository.findById(dto.getServerId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "서버를 선택해주세요."));
        }

        Item item = Item.builder()
                .seller(seller)
                .gameCategory(category)
                .gameServer(server)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .tradeType(TradeType.DIRECT)
                .build();

        Item savedItem = itemRepository.save(item);

        return ItemResDto.from(savedItem);
    }

    @Transactional(readOnly = true)
    public ItemDetailResDto getItemDetail(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (item.isDeletedByAdmin() || item.getStatus() == ItemStatus.DELETED) {
            throw new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }

        return ItemDetailResDto.from(item);
    }
}