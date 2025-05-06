package com.tradestore.infrastructure.repository;

import com.tradestore.domain.model.Trade;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TradeRepository extends MongoRepository<Trade, String> {
    @Query(value = "{ 'tradeId.tradeId': ?0 }", sort = "{ 'tradeId.version': -1 }")
    List<Trade> findByTradeIdOrderByVersionDesc(String tradeId);

    @Query("{ 'tradeId.tradeId': ?0, 'tradeId.version': ?1 }")
    Optional<Trade> findByTradeIdAndVersion(String tradeId, Integer version);

    @Query(value = "{ 'maturityDate': { $lt: ?0 }, 'expired': false }", sort = "{ 'tradeId.version': -1 }")
    List<Trade> findByMaturityDateBeforeAndExpiredFalse(LocalDate date);
} 