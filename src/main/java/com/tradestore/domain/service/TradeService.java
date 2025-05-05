package com.tradestore.domain.service;

import com.tradestore.domain.model.Trade;
import java.util.List;
import java.util.Optional;

public interface TradeService {
    Trade storeTrade(Trade trade);
    List<Trade> getAllTrades();
    Optional<Trade> getTradeById(String tradeId, Integer version);
    List<Trade> getTradesByTradeId(String tradeId);
    void updateExpiredTrades();
    
    // New methods for separate database access
    List<Trade> getMongoTrades();
    List<Trade> getPostgresTrades();
} 