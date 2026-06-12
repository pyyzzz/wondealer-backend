package com.wondealer.repository;

import com.wondealer.entity.Item;
import com.wondealer.entity.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
// JpaSpecificationExecutor<Item> 추가이유 페이징만 하려면 JpaRepository로 충분하다.
//하지만 gameId/categoryId/tradeType/keyword처럼 조건이 선택적으로 붙는 검색 + 페이징을 깔끔하게 하려면 JpaSpecificationExecutor가 좋다.
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByGameServer_Game_GameNameAndGameServer_ServerNameAndGameCategory_CategoryNameAndStatus(
            String gameName, String serverName, String categoryName, ItemStatus status
    );
}
