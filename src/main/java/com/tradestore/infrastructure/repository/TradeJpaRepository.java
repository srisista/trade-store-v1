package com.tradestore.infrastructure.repository;

import com.tradestore.infrastructure.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeJpaRepository extends JpaRepository<TradeEntity, Long> {
    
    @Query("SELECT t FROM TradeEntity t WHERE t.tradeId = ?1 ORDER BY t.version DESC")
    List<TradeEntity> findByTradeIdOrderByVersionDesc(String tradeId);

    default Optional<TradeEntity> findFirstByTradeIdOrderByVersionDesc(String tradeId) {
        return findByTradeIdOrderByVersionDesc(tradeId)
                .stream()
                .findFirst();
    }

    @Query("SELECT t FROM TradeEntity t WHERE t.tradeId = ?1 AND t.version = ?2")
    Optional<TradeEntity> findByTradeIdAndVersion(String tradeId, Integer version);

    @Query("SELECT t FROM TradeEntity t WHERE t.maturityDate < ?1 AND t.expired = false ORDER BY t.version DESC")
    List<TradeEntity> findByMaturityDateBeforeAndExpiredFalse(LocalDate date);
} 