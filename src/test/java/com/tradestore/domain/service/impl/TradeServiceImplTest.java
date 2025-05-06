package com.tradestore.domain.service.impl;

import com.tradestore.domain.exception.TradeException;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.infrastructure.repository.TradeRepository;
import com.tradestore.domain.service.TradeService;
import com.tradestore.infrastructure.entity.TradeEntity;
import com.tradestore.infrastructure.messaging.TradeEventProducer;
import com.tradestore.infrastructure.repository.TradeJpaRepository;
import com.tradestore.infrastructure.mapper.TradeMapper;
import com.tradestore.util.TestUtils;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceImplTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeJpaRepository jpaRepository;

    @Mock
    private TradeMapper tradeMapper;

    @Mock
    private TradeEventProducer eventProducer;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private Trade validTrade;
    private Trade expiredTrade;
    private TradeEntity validTradeEntity;

    @BeforeEach
    void setUp() {
        validTrade = TestUtils.createValidTrade();
        expiredTrade = TestUtils.createExpiredTrade();
        validTradeEntity = TestUtils.createValidTradeEntity();
        
        // Use lenient() for setup stubbings that might not be used in every test
        lenient().when(tradeRepository.save(any(Trade.class))).thenReturn(validTrade);
        lenient().when(tradeRepository.findByTradeIdAndVersion(anyString(), anyInt())).thenReturn(Optional.empty());
        lenient().when(tradeRepository.findAll()).thenReturn(Arrays.asList(validTrade, expiredTrade));
        lenient().when(tradeMapper.toEntity(any(Trade.class))).thenReturn(validTradeEntity);
        lenient().when(tradeMapper.toDomain(any(TradeEntity.class))).thenReturn(validTrade);
    }

    @Test
    void storeTrade_ValidTrade_ReturnsCreatedTrade() {
        // Act
        Trade result = tradeService.storeTrade(validTrade);

        // Assert
        assertNotNull(result);
        verify(tradeRepository).save(any(Trade.class));
        verify(jpaRepository).save(any(TradeEntity.class));
        verify(eventProducer).sendTradeEvent(any(Trade.class));
    }

    @Test
    void storeTrade_ExistingTrade_ThrowsException() {
        // Setup mocks
        when(tradeRepository.findByTradeIdAndVersion(eq(validTrade.getTradeId().getTradeId()), eq(validTrade.getTradeId().getVersion())))
                .thenReturn(Optional.of(validTrade));

        // Execute and verify
        assertThrows(TradeException.class, () -> tradeService.storeTrade(validTrade));
        verify(eventProducer, never()).sendTradeEvent(any(Trade.class));
    }

    @Test
    void storeTrade_PastMaturityDate_ThrowsException() {
        // Setup test data
        Trade pastMaturityTrade = validTrade.toBuilder()
                .maturityDate(LocalDate.now().minusDays(1))
                .build();

        // Execute and verify
        assertThrows(TradeException.class, () -> tradeService.storeTrade(pastMaturityTrade));
        verify(eventProducer, never()).sendTradeEvent(any(Trade.class));
    }

    @Test
    void getAllTrades_ReturnsAllTrades() {
        // Act
        List<Trade> result = tradeService.getAllTrades();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tradeRepository).findAll();
    }

    @Test
    void getTradeById_ExistingTrade_ReturnsTrade() {
        // Setup mocks
        when(tradeRepository.findByTradeIdAndVersion(
            validTrade.getTradeId().getTradeId(), 
            validTrade.getTradeId().getVersion()))
            .thenReturn(Optional.of(validTrade));

        // Act
        Optional<Trade> result = tradeService.getTradeById(
            validTrade.getTradeId().getTradeId(), 
            validTrade.getTradeId().getVersion());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validTrade.getTradeId().getTradeId(), result.get().getTradeId().getTradeId());
        verify(tradeRepository).findByTradeIdAndVersion(
            validTrade.getTradeId().getTradeId(), 
            validTrade.getTradeId().getVersion());
    }

    @Test
    void getTradesByTradeId_ReturnsAllVersions() {
        // Setup mocks
        List<Trade> expectedTrades = Arrays.asList(validTrade);
        when(tradeRepository.findByTradeIdOrderByVersionDesc(anyString()))
                .thenReturn(expectedTrades);

        // Execute
        List<Trade> result = tradeService.getTradesByTradeId("T1");

        // Verify
        assertEquals(expectedTrades.size(), result.size());
        verify(tradeRepository).findByTradeIdOrderByVersionDesc("T1");
    }

    @Test
    void updateExpiredTrades_ShouldMarkExpiredTrades() {
        // Arrange
        List<Trade> expiredTrades = Arrays.asList(expiredTrade);
        when(tradeRepository.findByMaturityDateBeforeAndExpiredFalse(any(LocalDate.class)))
            .thenReturn(expiredTrades);
        when(tradeRepository.save(any(Trade.class))).thenReturn(expiredTrade);

        // Act
        tradeService.updateExpiredTrades();

        // Assert
        verify(tradeRepository).findByMaturityDateBeforeAndExpiredFalse(any(LocalDate.class));
        verify(tradeRepository).save(any(Trade.class));
        verify(jpaRepository).save(any(TradeEntity.class));
        verify(eventProducer).sendTradeEvent(any(Trade.class));
    }

    @Test
    void getMongoTrades_ReturnsAllMongoTrades() {
        // Setup mocks
        List<Trade> expectedTrades = Arrays.asList(validTrade, expiredTrade);
        when(tradeRepository.findAll()).thenReturn(expectedTrades);

        // Execute
        List<Trade> result = tradeService.getMongoTrades();

        // Verify
        assertEquals(expectedTrades.size(), result.size());
        verify(tradeRepository).findAll();
    }

    @Test
    void getPostgresTrades_ReturnsAllPostgresTrades() {
        // Setup mocks
        List<TradeEntity> entities = Arrays.asList(validTradeEntity);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(tradeMapper.toDomain(any(TradeEntity.class))).thenReturn(validTrade);

        // Execute
        List<Trade> result = tradeService.getPostgresTrades();

        // Verify
        assertEquals(entities.size(), result.size());
        verify(jpaRepository).findAll();
        verify(tradeMapper).toDomain(any(TradeEntity.class));
    }
} 