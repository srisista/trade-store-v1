package com.tradestore.domain.service.impl;

import com.tradestore.domain.exception.TradeException;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.infrastructure.entity.TradeEntity;
import com.tradestore.infrastructure.mapper.TradeMapper;
import com.tradestore.infrastructure.repository.TradeJpaRepository;
import com.tradestore.infrastructure.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceImplTest {

    @Mock
    private TradeRepository mongoRepository;

    @Mock
    private TradeJpaRepository jpaRepository;

    @Mock
    private TradeMapper tradeMapper;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private Trade validTrade;
    private Trade expiredTrade;
    private TradeEntity validTradeEntity;

    @BeforeEach
    void setUp() {
        validTrade = Trade.builder()
                .tradeId(new TradeId("T1", 1))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        expiredTrade = Trade.builder()
                .tradeId(new TradeId("T2", 1))
                .counterPartyId("CP-2")
                .bookId("B2")
                .maturityDate(LocalDate.now().minusDays(1))
                .createdDate(LocalDate.now().minusDays(2))
                .expired(false)
                .build();

        validTradeEntity = new TradeEntity(); // Mock entity for testing
    }

    @Test
    void storeTrade_ValidTrade_ReturnsStoredTrade() {
        when(mongoRepository.findByTradeIdAndVersion(eq("T1"), eq(1)))
                .thenReturn(Optional.empty());
        when(mongoRepository.save(any(Trade.class))).thenReturn(validTrade);
        when(tradeMapper.toEntity(any(Trade.class))).thenReturn(validTradeEntity);

        Trade result = tradeService.storeTrade(validTrade);

        assertNotNull(result);
        assertEquals(validTrade.getTradeId().getTradeId(), result.getTradeId().getTradeId());
        verify(mongoRepository).save(any(Trade.class));
        verify(jpaRepository).save(any(TradeEntity.class));
    }

    @Test
    void storeTrade_ExistingTrade_ThrowsException() {
        when(mongoRepository.findByTradeIdAndVersion(eq("T1"), eq(1)))
                .thenReturn(Optional.of(validTrade));

        assertThrows(TradeException.class, () -> tradeService.storeTrade(validTrade));
    }

    @Test
    void storeTrade_PastMaturityDate_ThrowsException() {
        Trade pastMaturityTrade = validTrade.toBuilder()
                .maturityDate(LocalDate.now().minusDays(1))
                .build();

        assertThrows(TradeException.class, () -> tradeService.storeTrade(pastMaturityTrade));
    }

    @Test
    void getAllTrades_ReturnsAllTrades() {
        List<Trade> expectedTrades = Arrays.asList(validTrade, expiredTrade);
        when(mongoRepository.findAll()).thenReturn(expectedTrades);

        List<Trade> result = tradeService.getAllTrades();

        assertEquals(expectedTrades.size(), result.size());
        verify(mongoRepository).findAll();
    }

    @Test
    void getTradeById_ExistingTrade_ReturnsTrade() {
        String tradeId = "T1";
        Integer version = 1;
        when(mongoRepository.findByTradeIdAndVersion(eq(tradeId), eq(version)))
                .thenReturn(Optional.of(validTrade));

        Optional<Trade> result = tradeService.getTradeById(tradeId, version);

        assertTrue(result.isPresent());
        assertEquals(validTrade.getTradeId().getTradeId(), result.get().getTradeId().getTradeId());
        assertEquals(validTrade.getTradeId().getVersion(), result.get().getTradeId().getVersion());
    }

    @Test
    void getTradesByTradeId_ReturnsAllVersions() {
        List<Trade> expectedTrades = Arrays.asList(validTrade);
        when(mongoRepository.findByTradeIdOrderByVersionDesc(anyString()))
                .thenReturn(expectedTrades);

        List<Trade> result = tradeService.getTradesByTradeId("T1");

        assertEquals(expectedTrades.size(), result.size());
        verify(mongoRepository).findByTradeIdOrderByVersionDesc("T1");
    }

    @Test
    void updateExpiredTrades_UpdatesExpiredTrades() {
        List<Trade> expiredTrades = Arrays.asList(expiredTrade);
        when(mongoRepository.findByMaturityDateBeforeAndExpiredFalse(any(LocalDate.class)))
                .thenReturn(expiredTrades);
        when(tradeMapper.toEntity(any(Trade.class))).thenReturn(validTradeEntity);

        tradeService.updateExpiredTrades();

        verify(mongoRepository).save(any(Trade.class));
        verify(jpaRepository).save(any(TradeEntity.class));
    }

    @Test
    void getMongoTrades_ReturnsAllMongoTrades() {
        // Arrange
        List<Trade> expectedTrades = Arrays.asList(validTrade, expiredTrade);
        when(mongoRepository.findAll()).thenReturn(expectedTrades);

        // Act
        List<Trade> result = tradeService.getMongoTrades();

        // Assert
        assertEquals(expectedTrades.size(), result.size());
        verify(mongoRepository).findAll();
    }

    @Test
    void getPostgresTrades_ReturnsAllPostgresTrades() {
        // Arrange
        List<TradeEntity> entities = Arrays.asList(
            TradeEntity.builder()
                .tradeId("T1")
                .version(1)
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build(),
            TradeEntity.builder()
                .tradeId("T2")
                .version(1)
                .counterPartyId("CP-2")
                .bookId("B2")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build()
        );
        when(jpaRepository.findAll()).thenReturn(entities);
        when(tradeMapper.toDomain(any(TradeEntity.class))).thenAnswer(i -> {
            TradeEntity entity = i.getArgument(0);
            return Trade.builder()
                .tradeId(new TradeId(entity.getTradeId(), entity.getVersion()))
                .counterPartyId(entity.getCounterPartyId())
                .bookId(entity.getBookId())
                .maturityDate(entity.getMaturityDate())
                .createdDate(entity.getCreatedDate())
                .expired(entity.isExpired())
                .build();
        });

        // Act
        List<Trade> result = tradeService.getPostgresTrades();

        // Assert
        assertEquals(entities.size(), result.size());
        verify(jpaRepository).findAll();
        verify(tradeMapper, times(entities.size())).toDomain(any(TradeEntity.class));
    }
} 