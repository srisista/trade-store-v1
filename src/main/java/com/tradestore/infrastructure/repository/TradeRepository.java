package com.tradestore.infrastructure.repository;

import com.tradestore.domain.model.Trade;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends MongoRepository<Trade, String> {
    @Query(value = "{ 'tradeId.tradeId': ?0 }", sort = "{ 'tradeId.version': -1 }")
    List<Trade> findByTradeId_TradeIdOrderByTradeId_VersionDesc(String tradeId);

    default Optional<Trade> findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc(String tradeId) {
        return findByTradeId_TradeIdOrderByTradeId_VersionDesc(tradeId)
                .stream()
                .findFirst();
    }

    @Query(value = "{ 'tradeId.tradeId': ?0 }", sort = "{ 'tradeId.version': -1 }")
    List<Trade> findByTradeId_TradeId(String tradeId);

    @Query("{ 'tradeId.tradeId': ?0, 'tradeId.version': ?1 }")
    Optional<Trade> findByTradeId_TradeIdAndTradeId_Version(String tradeId, int version);

    @Query(value = "{ 'maturityDate': { $lt: ?0 }, 'expired': false }", sort = "{ 'tradeId.version': -1 }")
    List<Trade> findByMaturityDateBeforeAndExpiredFalse(LocalDate date);

    @Query("{ 'tradeId.tradeId': ?0, 'tradeId.version': ?1 }")
    Optional<Trade> findByTradeIdAndVersion(String tradeId, Integer version);

    @Query(value = "{ 'tradeId.tradeId': ?0 }", sort = "{ 'tradeId.version': -1 }")
    List<Trade> findByTradeIdOrderByVersionDesc(String tradeId);
} 