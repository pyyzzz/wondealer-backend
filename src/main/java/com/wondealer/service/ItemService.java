package com.wondealer.service;

import com.wondealer.dto.request.AuctionCreateReqDto;
import com.wondealer.dto.request.ItemCreateReqDto;
import com.wondealer.dto.request.ItemUpdateReqDto;
import com.wondealer.dto.response.AuctionCreateResDto;
import com.wondealer.dto.response.ItemCreateResDto;
import com.wondealer.dto.response.ItemDetailResDto;
import com.wondealer.dto.response.ItemListResDto;
import com.wondealer.entity.Auction;
import com.wondealer.entity.GameCategory;
import com.wondealer.entity.GameServer;
import com.wondealer.entity.Item;
import com.wondealer.entity.ItemStatus;
import com.wondealer.entity.Member;
import com.wondealer.entity.TradeType;
import com.wondealer.exception.CustomException;
import com.wondealer.repository.AuctionRepository;
import com.wondealer.entity.ItemImage;
import com.wondealer.repository.ItemImageRepository;
import java.util.List;
import com.wondealer.repository.GameCategoryRepository;
import com.wondealer.repository.GameServerRepository;
import com.wondealer.repository.ItemRepository;
import com.wondealer.repository.MemberRepository;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private static final Set<Integer> ALLOWED_AUCTION_DAYS = Set.of(1, 3, 7);

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final GameCategoryRepository gameCategoryRepository;
    private final GameServerRepository gameServerRepository;
    private final AuctionRepository auctionRepository;
    private final ItemImageRepository itemImageRepository;

    /**
     * 상품 목록을 조회한다.
     * gameId, serverId, categoryId, tradeType, keyword 조건이 있으면 조건을 붙이고 페이징해서 반환한다.
     */
    @Transactional(readOnly = true)
    public Page<ItemListResDto> getItems(
            Long gameId,
            Long serverId,
            Long categoryId,
            String tradeType,
            String keyword,
            Pageable pageable
    ) {
        Specification<Item> spec = activeSellingItems()
                .and(hasGameId(gameId))
                .and(hasServerId(serverId))
                .and(hasCategoryId(categoryId))
                .and(hasTradeType(tradeType))
                .and(containsKeyword(keyword));

        return itemRepository.findAll(spec, pageable)
                .map(ItemListResDto::from);
    }

    /**
     * 일반 판매 상품을 등록한다.
     * 판매자 상태, 카테고리, 서버를 검증한 뒤 DIRECT 타입의 Item을 저장한다.
     */
    public ItemCreateResDto createDirectItem(Long memberId, ItemCreateReqDto dto) {
        Member seller = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "로그인 후 상품 등록이 가능합니다."));

        if (!seller.isEmailVerified()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "이메일 인증 후 상품을 등록할 수 있습니다.");
        }

        if (seller.isBanned()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "정지된 회원은 상품을 등록할 수 없습니다.");
        }

        GameCategory category = gameCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "카테고리를 선택해주세요."));

        GameServer server = null;
        if (dto.getServerId() != null) {
            server = gameServerRepository.findById(dto.getServerId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "서버를 선택해주세요."));
            validateServerMatchesCategoryGame(server, category);
        }

        Item item = Item.builder()
                .seller(seller)
                .gameCategory(category)
                .gameServer(server)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getBasePrice())
                .tradeType(TradeType.DIRECT)
                .build();

        Item savedItem = itemRepository.save(item);

        // 이미지 URL 저장 (Firebase에서 업로드 후 전달된 URL)
        saveItemImages(savedItem, dto.getImageUrls());

        return ItemCreateResDto.from(savedItem);
    }

    /**
     * 경매 상품을 등록한다.
     * Item에는 상품 공통 정보와 AUCTION 거래 타입을 저장하고, Auction에는 경매 전용 정보를 저장한다.
     */
    public AuctionCreateResDto createAuctionItem(Long memberId, AuctionCreateReqDto dto) {
        validateAuctionDays(dto.getAuctionDays());
        validateInstantBuyPrice(dto.getStartPrice(), dto.getInstantBuyPrice());

        Member seller = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "로그인 후 상품 등록이 가능합니다."));

        if (!seller.isEmailVerified()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "이메일 인증 후 상품을 등록할 수 있습니다.");
        }

        if (seller.isBanned()) {
            throw new CustomException(HttpStatus.FORBIDDEN, "정지된 회원은 상품을 등록할 수 없습니다.");
        }

        GameCategory category = gameCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "카테고리를 선택해주세요."));

        GameServer server = null;
        if (dto.getServerId() != null) {
            server = gameServerRepository.findById(dto.getServerId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "서버를 선택해주세요."));
            validateServerMatchesCategoryGame(server, category);
        }

        Item item = Item.builder()
                .seller(seller)
                .gameCategory(category)
                .gameServer(server)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getStartPrice())
                .tradeType(TradeType.AUCTION)
                .build();

        Item savedItem = itemRepository.save(item);

        Auction auction = Auction.builder()
                .item(savedItem)
                .startPrice(dto.getStartPrice())
                .instantBuyPrice(dto.getInstantBuyPrice())
                .endTime(LocalDateTime.now().plusDays(dto.getAuctionDays()))
                .build();

        // 이미지 URL 저장 (Firebase에서 업로드 후 전달된 URL)
        saveItemImages(savedItem, dto.getImageUrls());

        Auction savedAuction = auctionRepository.save(auction);
        return AuctionCreateResDto.from(savedAuction);
    }

    /**
     * 상품 상세 정보를 조회한다.
     * 삭제된 상품은 조회하지 못하게 공통 조회 메서드에서 걸러낸다.
     */
    @Transactional(readOnly = true)
    public ItemDetailResDto getItemDetail(Long itemId) {
        Item item = getActiveItem(itemId);
        return ItemDetailResDto.from(item);
    }

    /**
     * 상품 정보를 수정한다.
     * 본인 상품인지, 아직 판매 중인 상품인지 확인한 뒤 변경 가능한 값만 수정한다.
     */
    public ItemCreateResDto updateItem(Long memberId, Long itemId, ItemUpdateReqDto dto) {
        Item item = getActiveItem(itemId);
        validateSeller(memberId, item);
        validateSelling(item);

        item.updateItem(dto.getTitle(), dto.getDescription(), dto.getBasePrice());

        return ItemCreateResDto.from(item);
    }

    /**
     * 상품을 삭제 상태로 변경한다.
     * 실제 DB row를 지우지 않고 status를 DELETED로 바꾸는 소프트 삭제 방식이다.
     */
    public void deleteItem(Long memberId, Long itemId) {
        Item item = getActiveItem(itemId);
        validateSeller(memberId, item);
        validateSelling(item);

        item.deleteBySeller();
    }


    /**
     * 이미지 URL 목록을 ItemImage로 저장한다.
     * Firebase에서 업로드된 URL을 순서대로 저장하며 첫 번째가 썸네일이 된다.
     */
    private void saveItemImages(Item item, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            itemImageRepository.save(ItemImage.builder()
                    .item(item)
                    .imageUrl(imageUrls.get(i))
                    .orderNum(i)
                    .build());
        }
    }

    /**
     * 조회 가능한 상품을 찾는다.
     * 존재하지 않거나 관리자/판매자에 의해 삭제된 상품이면 404 예외를 던진다.
     */
    private Item getActiveItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (item.isDeletedByAdmin() || item.getStatus() == ItemStatus.DELETED) {
            throw new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }

        return item;
    }

    /**
     * 로그인한 회원이 해당 상품의 판매자인지 확인한다.
     */
    private void validateSeller(Long memberId, Item item) {
        if (!item.getSeller().getId().equals(memberId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "본인 상품만 수정하거나 삭제할 수 있습니다.");
        }
    }

    /**
     * 상품이 수정/삭제 가능한 판매 중 상태인지 확인한다.
     */
    private void validateSelling(Item item) {
        if (item.getStatus() != ItemStatus.SELLING) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "판매 중인 상품만 수정하거나 삭제할 수 있습니다.");
        }
    }

    /**
     * 서버를 선택한 경우, 선택한 서버와 카테고리가 같은 게임에 속하는지 확인한다.
     */
    private void validateServerMatchesCategoryGame(GameServer server, GameCategory category) {
        Long serverGameId = server.getGame().getId();
        Long categoryGameId = category.getGame().getId();

        if (!serverGameId.equals(categoryGameId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "선택한 서버와 카테고리의 게임이 일치하지 않습니다.");
        }
    }

    private void validateAuctionDays(Integer auctionDays) {
        if (!ALLOWED_AUCTION_DAYS.contains(auctionDays)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "경매 기간은 1일, 3일, 7일만 선택할 수 있습니다.");
        }
    }

    private void validateInstantBuyPrice(Long startPrice, Long instantBuyPrice) {
        if (instantBuyPrice != null && instantBuyPrice <= startPrice) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "즉시 낙찰가는 경매 시작가보다 커야 합니다.");
        }
    }

    /**
     * 목록 조회 기본 조건이다.
     * 판매 중이고 관리자 삭제 처리되지 않은 상품만 노출한다.
     */
    private Specification<Item> activeSellingItems() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), ItemStatus.SELLING),
                cb.isFalse(root.get("isDeletedByAdmin"))
        );
    }

    /**
     * gameId가 전달된 경우 해당 게임에 속한 상품만 조회하는 조건을 만든다.
     */
    private Specification<Item> hasGameId(Long gameId) {
        return (root, query, cb) -> {
            if (gameId == null) {
                return cb.conjunction();
            }
            Join<Item, GameCategory> category = root.join("gameCategory");
            return cb.equal(category.get("game").get("id"), gameId);
        };
    }

    /**
     * serverId가 전달된 경우 해당 서버의 상품만 조회하는 조건을 만든다.
     */
    private Specification<Item> hasServerId(Long serverId) {
        return (root, query, cb) -> serverId == null
                ? cb.conjunction()
                : cb.equal(root.get("gameServer").get("id"), serverId);
    }

    /**
     * categoryId가 전달된 경우 해당 거래 종류의 상품만 조회하는 조건을 만든다.
     */
    private Specification<Item> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> categoryId == null
                ? cb.conjunction()
                : cb.equal(root.get("gameCategory").get("id"), categoryId);
    }

    /**
     * tradeType이 전달된 경우 DIRECT 또는 AUCTION 상품만 조회하는 조건을 만든다.
     */
    private Specification<Item> hasTradeType(String tradeType) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(tradeType)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("tradeType"), TradeType.valueOf(tradeType.toUpperCase()));
        };
    }

    /**
     * keyword가 전달된 경우 상품 제목에 keyword가 포함된 상품만 조회하는 조건을 만든다.
     */
    private Specification<Item> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }
}