package com.tradestore.infrastructure.repository;

import com.tradestore.config.TestContainersConfig;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@TestPropertySource(properties = {
    "spring.mongodb.embedded.version=4.0.21",
    "spring.data.mongodb.database=testdb"
})
class MongoTradeRepositoryTest {

    @Autowired
    private TradeRepository tradeRepository;

    private Trade trade;
    private TradeId tradeId;

    @BeforeEach
    void setUp() {
        tradeRepository.deleteAll();
        
        tradeId = new TradeId("T1", 1);
        trade = Trade.builder()
                .tradeId(tradeId)
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();
    }

    @Test
    void saveAndRetrieveTrade() {
        // Save trade
        Trade savedTrade = tradeRepository.save(trade);
        assertNotNull(savedTrade.getId());

        // Retrieve trade
        Optional<Trade> retrievedTrade = tradeRepository.findByTradeIdAndVersion(tradeId.getTradeId(), tradeId.getVersion());
        assertTrue(retrievedTrade.isPresent());
        assertEquals(tradeId.getTradeId(), retrievedTrade.get().getTradeId().getTradeId());
        assertEquals(tradeId.getVersion(), retrievedTrade.get().getTradeId().getVersion());
    }

    @Test
    void findTradeVersions() {
        // Save multiple versions of the same trade
        Trade tradeV1 = trade;
        Trade tradeV2 = Trade.builder()
                .tradeId(new TradeId("T1", 2))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        tradeRepository.save(tradeV1);
        tradeRepository.save(tradeV2);

        // Retrieve all versions
        List<Trade> versions = tradeRepository.findByTradeIdOrderByVersionDesc("T1");
        assertEquals(2, versions.size());
    }

    @Test
    void findExpiredTrades() {
        // Save expired and non-expired trades
        Trade expiredTrade = Trade.builder()
                .tradeId(new TradeId("T2", 1))
                .counterPartyId("CP-2")
                .bookId("B2")
                .maturityDate(LocalDate.now().minusDays(1))
                .createdDate(LocalDate.now().minusDays(2))
                .expired(false)
                .build();

        tradeRepository.save(trade);
        tradeRepository.save(expiredTrade);

        // Find expired trades
        List<Trade> expiredTrades = tradeRepository.findByMaturityDateBeforeAndExpiredFalse(LocalDate.now());
        assertEquals(1, expiredTrades.size());
        assertEquals("T2", expiredTrades.get(0).getTradeId().getTradeId());
    }
} 