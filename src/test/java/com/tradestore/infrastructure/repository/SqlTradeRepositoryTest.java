package com.tradestore.infrastructure.repository;

import com.tradestore.domain.model.Trade;
import com.tradestore.infrastructure.entity.TradeEntity;
import com.tradestore.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.format_sql=true",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true"
})
class SqlTradeRepositoryTest {

    @Autowired
    private TradeJpaRepository tradeRepository;

    private TradeEntity validTradeEntity;

    @BeforeEach
    void setUp() {
        validTradeEntity = TestUtils.createValidTradeEntity();
    }

    @Test
    void save_ValidTradeEntity_ReturnsSavedEntity() {
        // Act
        TradeEntity savedEntity = tradeRepository.save(validTradeEntity);

        // Assert
        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getId());
        assertEquals(validTradeEntity.getTradeId(), savedEntity.getTradeId());
    }

    @Test
    void findByTradeIdAndVersion_ExistingTrade_ReturnsTrade() {
        // Arrange
        TradeEntity savedEntity = tradeRepository.save(validTradeEntity);

        // Act
        Optional<TradeEntity> found = tradeRepository.findByTradeIdAndVersion(
            savedEntity.getTradeId().toString(), savedEntity.getVersion());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(savedEntity.getId(), found.get().getId());
    }

    @Test
    void findByTradeIdAndVersion_NonExistingTrade_ReturnsEmpty() {
        // Act
        Optional<TradeEntity> found = tradeRepository.findByTradeIdAndVersion("NON_EXISTING", 1);

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void findByTradeIdOrderByVersionDesc_ReturnsAllVersions() {
        // Arrange
        TradeEntity entity1 = TestUtils.createValidTradeEntity();
        entity1.setVersion(1);
        TradeEntity entity2 = TestUtils.createValidTradeEntity();
        entity2.setVersion(2);
        tradeRepository.save(entity1);
        tradeRepository.save(entity2);

        // Act
        List<TradeEntity> versions = tradeRepository.findByTradeIdOrderByVersionDesc(entity1.getTradeId());

        // Assert
        assertEquals(2, versions.size());
        assertEquals(2, versions.get(0).getVersion());
        assertEquals(1, versions.get(1).getVersion());
    }

    @Test
    void findByMaturityDateBeforeAndExpiredFalse_ReturnsExpiredTrades() {
        // Arrange
        TradeEntity expiredTrade = validTradeEntity.toBuilder()
            .maturityDate(LocalDate.now().minusDays(1))
            .expired(false)
            .build();
        tradeRepository.save(expiredTrade);

        // Act
        List<TradeEntity> expiredTrades = tradeRepository.findByMaturityDateBeforeAndExpiredFalse(LocalDate.now());

        // Assert
        assertFalse(expiredTrades.isEmpty());
        assertTrue(expiredTrades.stream().anyMatch(trade -> 
            trade.getTradeId().equals(expiredTrade.getTradeId())));
    }
} 