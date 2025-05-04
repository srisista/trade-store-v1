package com.tradestore.infrastructure.repository;

import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class TradeRepositoryIntegrationTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Trade.class);
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(Trade.class);
    }

    @Test
    void saveAndFindTrade_ShouldWorkCorrectly() {
        // Arrange
        TradeId tradeId = new TradeId("T1", 1);
        Trade trade = Trade.builder()
                .tradeId(tradeId)
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        // Act
        Trade savedTrade = tradeRepository.save(trade);
        Optional<Trade> foundTrade = tradeRepository.findByTradeId_TradeIdAndTradeId_Version(tradeId.getTradeId(), tradeId.getVersion());

        // Assert
        assertTrue(foundTrade.isPresent());
        assertEquals(tradeId.getTradeId(), foundTrade.get().getTradeId().getTradeId());
        assertEquals(tradeId.getVersion(), foundTrade.get().getTradeId().getVersion());
    }

    @Test
    void findLatestVersion_ShouldReturnLatestVersion() {
        // Arrange
        String tradeId = "T1";
        Trade trade1 = Trade.builder()
                .tradeId(new TradeId(tradeId, 1))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        Trade trade2 = Trade.builder()
                .tradeId(new TradeId(tradeId, 2))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        tradeRepository.save(trade1);
        tradeRepository.save(trade2);

        // Act
        Optional<Trade> latestTrade = tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc(tradeId);

        // Assert
        assertTrue(latestTrade.isPresent());
        assertEquals(2, latestTrade.get().getTradeId().getVersion());
    }

    @Test
    void findByMaturityDateBeforeAndExpiredFalse_ShouldReturnExpiredTrades() {
        // Arrange
        LocalDate today = LocalDate.now();
        Trade expiredTrade = Trade.builder()
                .tradeId(new TradeId("T1", 1))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(today.minusDays(1))
                .createdDate(today)
                .expired(false)
                .build();

        Trade nonExpiredTrade = Trade.builder()
                .tradeId(new TradeId("T2", 1))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(today.plusDays(1))
                .createdDate(today)
                .expired(false)
                .build();

        tradeRepository.save(expiredTrade);
        tradeRepository.save(nonExpiredTrade);

        // Act
        List<Trade> expiredTrades = tradeRepository.findByMaturityDateBeforeAndExpiredFalse(today);

        // Assert
        assertEquals(1, expiredTrades.size());
        assertEquals("T1", expiredTrades.get(0).getTradeId().getTradeId());
    }

    @Test
    void findByTradeId_TradeId_ShouldReturnAllVersions() {
        // Arrange
        String tradeId = "T1";
        Trade trade1 = Trade.builder()
                .tradeId(new TradeId(tradeId, 1))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        Trade trade2 = Trade.builder()
                .tradeId(new TradeId(tradeId, 2))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        tradeRepository.save(trade1);
        tradeRepository.save(trade2);

        // Act
        List<Trade> foundTrades = tradeRepository.findByTradeId_TradeId(tradeId);

        // Assert
        assertEquals(2, foundTrades.size());
        assertTrue(foundTrades.stream().anyMatch(t -> t.getTradeId().getVersion() == 1));
        assertTrue(foundTrades.stream().anyMatch(t -> t.getTradeId().getVersion() == 2));
    }
} 