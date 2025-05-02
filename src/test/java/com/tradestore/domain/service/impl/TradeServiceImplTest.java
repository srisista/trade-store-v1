package com.tradestore.domain.service.impl;

import com.tradestore.domain.exception.TradeException;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.infrastructure.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceImplTest {

    @Mock
    private TradeRepository tradeRepository;

    private TradeServiceImpl tradeService;

    private Trade trade;
    private TradeId tradeId;

    @BeforeEach
    void setUp() {
        tradeService = new TradeServiceImpl(tradeRepository);
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
    void storeTrade_ValidTrade_ShouldStoreTrade() {
        // Arrange
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        // Act
        Trade result = tradeService.storeTrade(trade);

        // Assert
        assertNotNull(result);
        assertEquals(tradeId.getTradeId(), result.getTradeId().getTradeId());
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void storeTrade_InvalidMaturityDate_ShouldThrowException() {
        // Arrange
        trade.setMaturityDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(TradeException.class, () -> tradeService.storeTrade(trade));
        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    void storeTrade_LowerVersion_ShouldThrowException() {
        // Arrange
        Trade existingTrade = Trade.builder()
                .tradeId(new TradeId("T1", 2))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1"))
                .thenReturn(Optional.of(existingTrade));

        // Act & Assert
        assertThrows(TradeException.class, () -> tradeService.storeTrade(trade));
        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    void getAllTrades_ShouldReturnAllTrades() {
        // Arrange
        List<Trade> trades = Arrays.asList(trade);
        when(tradeRepository.findAll()).thenReturn(trades);

        // Act
        List<Trade> result = tradeService.getAllTrades();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tradeId.getTradeId(), result.get(0).getTradeId().getTradeId());
    }

    @Test
    void getTradeById_ExistingTrade_ShouldReturnTrade() {
        // Arrange
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1"))
                .thenReturn(Optional.of(trade));

        // Act
        Optional<Trade> result = tradeService.getTradeById("T1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(tradeId.getTradeId(), result.get().getTradeId().getTradeId());
    }

    @Test
    void getTradeById_NonExistingTrade_ShouldReturnEmpty() {
        // Arrange
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Trade> result = tradeService.getTradeById("T1");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getTradesByTradeId_ShouldReturnAllVersions() {
        // Arrange
        List<Trade> trades = Arrays.asList(trade);
        when(tradeRepository.findByTradeId_TradeId("T1")).thenReturn(trades);

        // Act
        List<Trade> result = tradeService.getTradesByTradeId("T1");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tradeId.getTradeId(), result.get(0).getTradeId().getTradeId());
    }

    @Test
    void updateExpiredTrades_ShouldUpdateExpiredTrades() {
        // Arrange
        List<Trade> expiredTrades = Arrays.asList(trade);
        when(tradeRepository.findByMaturityDateBeforeAndExpiredFalse(LocalDate.now()))
                .thenReturn(expiredTrades);
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        // Act
        tradeService.updateExpiredTrades();

        // Assert
        verify(tradeRepository).save(any(Trade.class));
    }
} 