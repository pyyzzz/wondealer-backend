package com.wondealer.repository;

import com.wondealer.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 💡 설계서 스펙 반영: ItemType 대신 GameCategory의 categoryName('아이템'/'게임머니'/'계정') 버튼 필터링 구현
    // 일반 아이템 거래소 화면용 조회 메서드
    List<Item> findByGameServer_Game_GameNameAndGameServer_ServerNameAndGameCategory_CategoryNameAndStatus(
            String gameName, String serverName, String categoryName, String status
    );
}