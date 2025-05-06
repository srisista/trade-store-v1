package com.tradestore.infrastructure.scheduler;

import com.tradestore.domain.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TradeExpirationScheduler {
    private final Logger logger;
    private final TradeService tradeService;

    public TradeExpirationScheduler(TradeService tradeService) {
        this.logger = LoggerFactory.getLogger(TradeExpirationScheduler.class);
        this.tradeService = tradeService;
    }

    @Scheduled(cron = "${trade.expiration.cron:0 0 0 * * ?}")
    public void updateExpiredTrades() {
        try {
            tradeService.updateExpiredTrades();
        } catch (Exception e) {
            logger.error("Error updating expired trades: {}", e.getMessage(), e);
            // Don't rethrow the exception to prevent scheduler from stopping
        }
    }
} 