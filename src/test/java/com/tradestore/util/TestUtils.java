package com.tradestore.util;

import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.infrastructure.entity.TradeEntity;

import java.time.LocalDate;

public class TestUtils {

    private TestUtils() {
        // Utility class - prevent instantiation
    }

    public static Trade createValidTrade() {
        return Trade.builder()
                .tradeId(new TradeId("T1", 1))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();
    }

    public static Trade createExpiredTrade() {
        return Trade.builder()
                .tradeId(new TradeId("T2", 1))
                .counterPartyId("CP-2")
                .bookId("B2")
                .maturityDate(LocalDate.now().minusDays(1))
                .createdDate(LocalDate.now().minusDays(2))
                .expired(false)
                .build();
    }

    public static TradeEntity createValidTradeEntity() {
        return TradeEntity.builder()
                .tradeId("T1")
                .version(1)
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();
    }
} 