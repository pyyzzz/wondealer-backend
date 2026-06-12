package com.wondealer.repository;

import com.wondealer.entity.GameCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameCategoryRepository extends JpaRepository<GameCategory, Long> {
    List<GameCategory> findByGameId(Long gameId);
}
