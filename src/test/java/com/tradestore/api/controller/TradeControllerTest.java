package com.tradestore.api.controller;

import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.domain.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeControllerTest {

    @Mock
    private TradeService tradeService;

    @InjectMocks
    private TradeController tradeController;

    private Trade trade;
    private TradeId tradeId;

    @BeforeEach
    void setUp() {
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
    void storeTrade_ValidTrade_ShouldReturnCreated() {
        // Arrange
        when(tradeService.storeTrade(any(Trade.class))).thenReturn(trade);

        // Act
        ResponseEntity<Trade> response = tradeController.storeTrade(trade);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(tradeId.getTradeId(), response.getBody().getTradeId().getTradeId());
    }

    @Test
    void getAllTrades_ShouldReturnAllTrades() {
        // Arrange
        List<Trade> trades = Arrays.asList(trade);
        when(tradeService.getAllTrades()).thenReturn(trades);

        // Act
        ResponseEntity<List<Trade>> response = tradeController.getAllTrades();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(tradeId.getTradeId(), response.getBody().get(0).getTradeId().getTradeId());
    }

    @Test
    void getTradeById_ShouldReturnTrade() {
        // Arrange
        when(tradeService.getTradeById(anyString())).thenReturn(Optional.of(trade));

        // Act
        ResponseEntity<Trade> response = tradeController.getTradeById("T1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(tradeId.getTradeId(), response.getBody().getTradeId().getTradeId());
    }

    @Test
    void getTradeById_NotFound_ShouldReturnNotFound() {
        // Arrange
        when(tradeService.getTradeById(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Trade> response = tradeController.getTradeById("T1");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getTradeVersions_ShouldReturnAllVersions() {
        // Arrange
        List<Trade> trades = Arrays.asList(trade);
        when(tradeService.getTradesByTradeId(anyString())).thenReturn(trades);

        // Act
        ResponseEntity<List<Trade>> response = tradeController.getTradeVersions("T1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(tradeId.getTradeId(), response.getBody().get(0).getTradeId().getTradeId());
    }
} 