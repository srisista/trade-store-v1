package com.tradestore.domain.service.impl;

import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.domain.service.TradeService;
import com.tradestore.infrastructure.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;

    @Autowired
    public TradeServiceImpl(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Override
    public Trade storeTrade(Trade trade) {
        // Validate maturity date
        if (trade.getMaturityDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Maturity date cannot be in the past");
        }

        // Get the latest version of the trade
        Optional<Trade> latestTrade = tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc(trade.getTradeId().getTradeId());
        
        // Validate version
        if (latestTrade.isPresent() && trade.getTradeId().getVersion() < latestTrade.get().getTradeId().getVersion()) {
            throw new IllegalArgumentException("Cannot store trade with lower version than existing trade");
        }
        
        // Set created date
        trade.setCreatedDate(LocalDate.now());
        
        // Save the trade
        return tradeRepository.save(trade);
    }

    @Override
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    @Override
    public Optional<Trade> getTradeById(String tradeId) {
        return tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc(tradeId);
    }

    @Override
    public List<Trade> getTradesByTradeId(String tradeId) {
        return tradeRepository.findByTradeId_TradeId(tradeId);
    }

    @Override
    public void updateExpiredTrades() {
        LocalDate today = LocalDate.now();
        List<Trade> expiredTrades = tradeRepository.findByMaturityDateBeforeAndExpiredFalse(today);
        
        for (Trade trade : expiredTrades) {
            trade.setExpired(true);
            tradeRepository.save(trade);
        }
    }
} 