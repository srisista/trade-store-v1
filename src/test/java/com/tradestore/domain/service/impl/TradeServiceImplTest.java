package com.tradestore.domain.service.impl;

import com.tradestore.domain.exception.TradeException;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceImplTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private Trade validTrade;
    private Trade existingTrade;

    @BeforeEach
    void setUp() {
        validTrade = new Trade();
        validTrade.setTradeId(new TradeId("T1", 1));
        validTrade.setCounterPartyId("CP-1");
        validTrade.setBookId("B1");
        validTrade.setMaturityDate(LocalDate.now().plusDays(1));
        validTrade.setExpired(false);

        existingTrade = new Trade();
        existingTrade.setTradeId(new TradeId("T1", 2));
        existingTrade.setCounterPartyId("CP-1");
        existingTrade.setBookId("B1");
        existingTrade.setMaturityDate(LocalDate.now().plusDays(1));
        existingTrade.setExpired(false);
    }

    @Test
    void storeTrade_ValidTrade_ReturnsStoredTrade() {
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1"))
            .thenReturn(Optional.empty());
        when(tradeRepository.save(any(Trade.class))).thenReturn(validTrade);

        Trade result = tradeService.storeTrade(validTrade);

        assertNotNull(result);
        assertEquals("T1", result.getTradeId().getTradeId());
        assertEquals(1, result.getTradeId().getVersion());
        assertNotNull(result.getCreatedDate());
        verify(tradeRepository, times(1)).save(any(Trade.class));
    }

    @Test
    void storeTrade_InvalidMaturityDate_ThrowsException() {
        validTrade.setMaturityDate(LocalDate.now().minusDays(1));

        TradeException exception = assertThrows(TradeException.class, 
            () -> tradeService.storeTrade(validTrade));
        assertEquals("Maturity date cannot be in the past", exception.getMessage());
        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    void storeTrade_LowerVersion_ThrowsException() {
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1"))
            .thenReturn(Optional.of(existingTrade));

        TradeException exception = assertThrows(TradeException.class, 
            () -> tradeService.storeTrade(validTrade));
        assertEquals("Cannot store trade with lower version than existing trade", exception.getMessage());
        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    void storeTrade_RepositoryError_ThrowsException() {
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1"))
            .thenThrow(new RuntimeException("Database error"));

        TradeException exception = assertThrows(TradeException.class, 
            () -> tradeService.storeTrade(validTrade));
        assertTrue(exception.getMessage().contains("Error storing trade"));
        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    void getAllTrades_ReturnsAllTrades() {
        List<Trade> trades = Arrays.asList(validTrade, existingTrade);
        when(tradeRepository.findAll()).thenReturn(trades);

        List<Trade> result = tradeService.getAllTrades();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tradeRepository, times(1)).findAll();
    }

    @Test
    void getAllTrades_RepositoryError_ThrowsException() {
        when(tradeRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        TradeException exception = assertThrows(TradeException.class, 
            () -> tradeService.getAllTrades());
        assertTrue(exception.getMessage().contains("Error retrieving trades"));
    }

    @Test
    void getTradeById_ExistingTrade_ReturnsTrade() {
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1"))
            .thenReturn(Optional.of(existingTrade));

        Optional<Trade> result = tradeService.getTradeById("T1");

        assertTrue(result.isPresent());
        assertEquals("T1", result.get().getTradeId().getTradeId());
        assertEquals(2, result.get().getTradeId().getVersion());
        verify(tradeRepository, times(1)).findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1");
    }

    @Test
    void getTradeById_NonExistingTrade_ReturnsEmpty() {
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("NONEXISTENT"))
            .thenReturn(Optional.empty());

        Optional<Trade> result = tradeService.getTradeById("NONEXISTENT");

        assertTrue(result.isEmpty());
        verify(tradeRepository, times(1)).findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("NONEXISTENT");
    }

    @Test
    void getTradeById_RepositoryError_ThrowsException() {
        when(tradeRepository.findFirstByTradeId_TradeIdOrderByTradeId_VersionDesc("T1"))
            .thenThrow(new RuntimeException("Database error"));

        TradeException exception = assertThrows(TradeException.class, 
            () -> tradeService.getTradeById("T1"));
        assertTrue(exception.getMessage().contains("Error retrieving trade by ID"));
    }

    @Test
    void getTradesByTradeId_ReturnsAllVersions() {
        List<Trade> trades = Arrays.asList(validTrade, existingTrade);
        when(tradeRepository.findByTradeId_TradeId("T1")).thenReturn(trades);

        List<Trade> result = tradeService.getTradesByTradeId("T1");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(tradeRepository, times(1)).findByTradeId_TradeId("T1");
    }

    @Test
    void getTradesByTradeId_RepositoryError_ThrowsException() {
        when(tradeRepository.findByTradeId_TradeId("T1"))
            .thenThrow(new RuntimeException("Database error"));

        TradeException exception = assertThrows(TradeException.class, 
            () -> tradeService.getTradesByTradeId("T1"));
        assertTrue(exception.getMessage().contains("Error retrieving trade versions"));
    }

    @Test
    void updateExpiredTrades_UpdatesExpiredTrades() {
        List<Trade> expiredTrades = Arrays.asList(validTrade);
        when(tradeRepository.findByMaturityDateBeforeAndExpiredFalse(any(LocalDate.class)))
            .thenReturn(expiredTrades);
        when(tradeRepository.save(any(Trade.class))).thenReturn(validTrade);

        tradeService.updateExpiredTrades();

        assertTrue(validTrade.isExpired());
        verify(tradeRepository, times(1)).save(validTrade);
    }

    @Test
    void updateExpiredTrades_NoExpiredTrades_DoesNothing() {
        when(tradeRepository.findByMaturityDateBeforeAndExpiredFalse(any(LocalDate.class)))
            .thenReturn(List.of());

        tradeService.updateExpiredTrades();

        verify(tradeRepository, never()).save(any(Trade.class));
    }

    @Test
    void updateExpiredTrades_RepositoryError_ThrowsException() {
        when(tradeRepository.findByMaturityDateBeforeAndExpiredFalse(any(LocalDate.class)))
            .thenThrow(new RuntimeException("Database error"));

        TradeException exception = assertThrows(TradeException.class, 
            () -> tradeService.updateExpiredTrades());
        assertTrue(exception.getMessage().contains("Error updating expired trades"));
    }
} 