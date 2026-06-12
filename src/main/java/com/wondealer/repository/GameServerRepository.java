package com.wondealer.repository;

import com.wondealer.entity.GameServer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameServerRepository extends JpaRepository<GameServer, Long> {
    List<GameServer> findByGameId(Long gameId);
    List<GameServer> findByGameIdAndIsActiveTrue(Long gameId);
}
