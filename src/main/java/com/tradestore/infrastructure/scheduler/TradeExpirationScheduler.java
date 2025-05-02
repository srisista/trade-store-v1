package com.tradestore.infrastructure.scheduler;

import com.tradestore.domain.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TradeExpirationScheduler {

    private final TradeService tradeService;

    @Autowired
    public TradeExpirationScheduler(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void updateExpiredTrades() {
        tradeService.updateExpiredTrades();
    }
} 