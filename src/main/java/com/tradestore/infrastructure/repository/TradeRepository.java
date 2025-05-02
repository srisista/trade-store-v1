package com.tradestore.infrastructure.repository;

import com.tradestore.domain.model.Trade;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends MongoRepository<Trade, String> {
    @Query(value = "{ 'tradeId.tradeId': ?0 }", sort = "{ 'tradeId.version': -1 }")
    Optional<Trade> findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc(String tradeId);

    @Query("{ 'tradeId.tradeId': ?0 }")
    List<Trade> findByTradeId_TradeId(String tradeId);

    @Query("{ 'tradeId.tradeId': ?0, 'tradeId.version': ?1 }")
    Optional<Trade> findByTradeId_TradeIdAndTradeId_Version(String tradeId, int version);

    @Query("{ 'maturityDate': { $lt: ?0 }, 'expired': false }")
    List<Trade> findByMaturityDateBeforeAndExpiredFalse(LocalDate date);
} 