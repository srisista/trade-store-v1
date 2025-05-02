package com.tradestore.domain.service.impl;

import com.tradestore.domain.exception.TradeException;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.service.TradeService;
import com.tradestore.infrastructure.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepository tradeRepository;

    public TradeServiceImpl(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Override
    public Trade storeTrade(Trade trade) {
        try {
            // Validate maturity date
            if (trade.getMaturityDate().isBefore(LocalDate.now())) {
                throw new TradeException("Maturity date cannot be in the past");
            }

            // Get the latest version of the trade
            Optional<Trade> latestTrade = tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc(trade.getTradeId().getTradeId());
            
            // Validate version
            if (latestTrade.isPresent() && trade.getTradeId().getVersion() < latestTrade.get().getTradeId().getVersion()) {
                throw new TradeException("Cannot store trade with lower version than existing trade");
            }
            
            // Set created date
            trade.setCreatedDate(LocalDate.now());
            
            // Save the trade
            return tradeRepository.save(trade);
        } catch (Exception e) {
            if (e instanceof TradeException) {
                throw e;
            }
            throw new TradeException("Error storing trade: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Trade> getAllTrades() {
        try {
            return tradeRepository.findAll();
        } catch (Exception e) {
            throw new TradeException("Error retrieving trades: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Trade> getTradeById(String tradeId) {
        try {
            return tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc(tradeId);
        } catch (Exception e) {
            throw new TradeException("Error retrieving trade by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Trade> getTradesByTradeId(String tradeId) {
        try {
            return tradeRepository.findByTradeId_TradeId(tradeId);
        } catch (Exception e) {
            throw new TradeException("Error retrieving trade versions: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateExpiredTrades() {
        try {
            LocalDate today = LocalDate.now();
            List<Trade> expiredTrades = tradeRepository.findByMaturityDateBeforeAndExpiredFalse(today);
            
            for (Trade trade : expiredTrades) {
                trade.setExpired(true);
                tradeRepository.save(trade);
            }
        } catch (Exception e) {
            throw new TradeException("Error updating expired trades: " + e.getMessage(), e);
        }
    }
} 