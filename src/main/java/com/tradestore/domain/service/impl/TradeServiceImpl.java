package com.tradestore.domain.service.impl;

import com.tradestore.domain.exception.TradeException;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.service.TradeService;
import com.tradestore.infrastructure.entity.TradeEntity;
import com.tradestore.infrastructure.mapper.TradeMapper;
import com.tradestore.infrastructure.messaging.TradeEventProducer;
import com.tradestore.infrastructure.repository.TradeJpaRepository;
import com.tradestore.infrastructure.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final TradeRepository mongoRepository;
    private final TradeJpaRepository jpaRepository;
    private final TradeMapper tradeMapper;
    private final TradeEventProducer tradeEventProducer;

    @Override
    @Transactional
    public Trade storeTrade(Trade trade) {
        log.info("Storing trade: {}", trade);
        try {
            // Validate maturity date
            if (trade.getMaturityDate().isBefore(LocalDate.now())) {
                throw new TradeException("Maturity date cannot be in the past");
            }

            // Check for existing trade with same ID and version
            Optional<Trade> existingTrade = mongoRepository.findByTradeIdAndVersion(
                trade.getTradeId().getTradeId(),
                trade.getTradeId().getVersion()
            );

            if (existingTrade.isPresent()) {
                throw new TradeException("Trade with same ID and version already exists");
            }

            // Save to MongoDB
            Trade savedTrade = mongoRepository.save(trade);
            
            // Save to PostgreSQL
            jpaRepository.save(tradeMapper.toEntity(trade));

            // Send to Kafka
            tradeEventProducer.sendTradeEvent(savedTrade);

            log.info("Trade stored successfully: {}", savedTrade);
            return savedTrade;
        } catch (Exception e) {
            log.error("Error storing trade: {}", e.getMessage());
            throw new TradeException("Error storing trade: " + e.getMessage());
        }
    }

    @Override
    public List<Trade> getAllTrades() {
        log.info("Fetching all trades");
        return mongoRepository.findAll();
    }

    @Override
    public Optional<Trade> getTradeById(String tradeId, Integer version) {
        log.info("Fetching trade with ID: {} and version: {}", tradeId, version);
        return mongoRepository.findByTradeIdAndVersion(tradeId, version);
    }

    @Override
    public List<Trade> getTradesByTradeId(String tradeId) {
        log.info("Fetching all versions of trade with ID: {}", tradeId);
        return mongoRepository.findByTradeIdOrderByVersionDesc(tradeId);
    }

    @Override
    @Transactional
    public void updateExpiredTrades() {
        log.info("Updating expired trades");
        try {
            LocalDate today = LocalDate.now();
            List<Trade> expiredTrades = mongoRepository.findByMaturityDateBeforeAndExpiredFalse(today);
            
            for (Trade trade : expiredTrades) {
                trade.setExpired(true);
                Trade savedTrade = mongoRepository.save(trade);
                jpaRepository.save(tradeMapper.toEntity(trade));
                tradeEventProducer.sendTradeEvent(savedTrade);
                log.info("Marked trade as expired: {}", trade);
            }
        } catch (Exception e) {
            log.error("Error updating expired trades: {}", e.getMessage());
            throw new TradeException("Error updating expired trades: " + e.getMessage());
        }
    }

    @Override
    public List<Trade> getMongoTrades() {
        log.info("Fetching all trades from MongoDB");
        return mongoRepository.findAll();
    }

    @Override
    public List<Trade> getPostgresTrades() {
        log.info("Fetching all trades from PostgreSQL");
        return jpaRepository.findAll().stream()
                .map(tradeMapper::toDomain)
                .toList();
    }
} 