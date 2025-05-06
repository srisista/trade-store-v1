package com.tradestore.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradestore.domain.exception.TradeException;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.domain.service.TradeService;
import com.tradestore.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Trade validTrade;

    @BeforeEach
    void setUp() {
        validTrade = TestUtils.createValidTrade();
    }

    @Test
    void storeTrade_ValidTrade_ReturnsCreatedTrade() throws Exception {
        // Arrange
        when(tradeService.storeTrade(any(Trade.class))).thenReturn(validTrade);

        // Act & Assert
        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTrade)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tradeId.tradeId").value("T1"))
                .andExpect(jsonPath("$.tradeId.version").value(1));
    }

    @Test
    void storeTrade_InvalidTrade_ReturnsBadRequest() throws Exception {
        // Arrange
        Trade invalidTrade = validTrade.toBuilder()
                .maturityDate(LocalDate.now().minusDays(1))
                .build();
        
        when(tradeService.storeTrade(any(Trade.class)))
                .thenThrow(new TradeException("Maturity date cannot be in the past"));

        // Act & Assert
        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTrade)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Maturity date cannot be in the past"));
    }

    @Test
    void getAllTrades_ReturnsListOfTrades() throws Exception {
        // Arrange
        Trade secondTrade = TestUtils.createValidTrade().toBuilder()
                .tradeId(new TradeId("T2", 1))
                .counterPartyId("CP-2")
                .bookId("B2")
                .build();
                
        List<Trade> trades = Arrays.asList(validTrade, secondTrade);
        when(tradeService.getAllTrades()).thenReturn(trades);

        // Act & Assert
        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeId.tradeId").value("T1"))
                .andExpect(jsonPath("$[1].tradeId.tradeId").value("T2"));
    }

    @Test
    void getTrade_ExistingTrade_ReturnsOk() throws Exception {
        when(tradeService.getTradeById("T1", 1))
                .thenReturn(Optional.of(validTrade));

        mockMvc.perform(get("/api/trades/T1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId.tradeId").value("T1"))
                .andExpect(jsonPath("$.tradeId.version").value(1));
    }

    @Test
    void getTrade_NonExistingTrade_ReturnsNotFound() throws Exception {
        when(tradeService.getTradeById("T1", 1))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/trades/T1/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTradesByTradeId_ReturnsListOfTrades() throws Exception {
        // Arrange
        Trade secondVersion = validTrade.toBuilder()
                .tradeId(new TradeId("T1", 2))
                .build();
                
        List<Trade> trades = Arrays.asList(validTrade, secondVersion);
        when(tradeService.getTradesByTradeId("T1")).thenReturn(trades);

        // Act & Assert
        mockMvc.perform(get("/api/trades/T1/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeId.version").value(1))
                .andExpect(jsonPath("$[1].tradeId.version").value(2));
    }

    @Test
    void getMongoTrades_ReturnsMongoTrades() throws Exception {
        // Arrange
        List<Trade> expectedTrades = Arrays.asList(validTrade);
        when(tradeService.getMongoTrades()).thenReturn(expectedTrades);

        // Act & Assert
        mockMvc.perform(get("/api/trades/mongo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeId.tradeId").value("T1"))
                .andExpect(jsonPath("$[0].tradeId.version").value(1));
    }

    @Test
    void getPostgresTrades_ReturnsPostgresTrades() throws Exception {
        // Arrange
        List<Trade> expectedTrades = Arrays.asList(validTrade);
        when(tradeService.getPostgresTrades()).thenReturn(expectedTrades);

        // Act & Assert
        mockMvc.perform(get("/api/trades/postgres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeId.tradeId").value("T1"))
                .andExpect(jsonPath("$[0].tradeId.version").value(1));
    }
} 