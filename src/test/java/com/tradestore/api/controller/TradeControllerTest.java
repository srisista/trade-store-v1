package com.tradestore.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradestore.domain.exception.TradeException;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.domain.service.TradeService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TradeService tradeService;

    @Test
    void storeTrade_ValidTrade_ReturnsCreatedTrade() throws Exception {
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
        when(tradeService.storeTrade(any(Trade.class))).thenReturn(trade);

        // Act & Assert
        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trade)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tradeId.tradeId").value("T1"))
                .andExpect(jsonPath("$.tradeId.version").value(1));
    }

    @Test
    void storeTrade_InvalidTrade_ReturnsBadRequest() throws Exception {
        // Arrange
        TradeId tradeId = new TradeId("T1", 1);
        Trade trade = Trade.builder()
                .tradeId(tradeId)
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().minusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();
        when(tradeService.storeTrade(any(Trade.class)))
                .thenThrow(new TradeException("Maturity date cannot be in the past"));

        // Act & Assert
        mockMvc.perform(post("/api/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trade)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Maturity date cannot be in the past"));
    }

    @Test
    void getAllTrades_ReturnsListOfTrades() throws Exception {
        // Arrange
        TradeId tradeId1 = new TradeId("T1", 1);
        TradeId tradeId2 = new TradeId("T2", 1);
        List<Trade> trades = Arrays.asList(
                Trade.builder()
                        .tradeId(tradeId1)
                        .counterPartyId("CP-1")
                        .bookId("B1")
                        .maturityDate(LocalDate.now().plusDays(1))
                        .createdDate(LocalDate.now())
                        .expired(false)
                        .build(),
                Trade.builder()
                        .tradeId(tradeId2)
                        .counterPartyId("CP-2")
                        .bookId("B2")
                        .maturityDate(LocalDate.now().plusDays(1))
                        .createdDate(LocalDate.now())
                        .expired(false)
                        .build()
        );
        when(tradeService.getAllTrades()).thenReturn(trades);

        // Act & Assert
        mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeId.tradeId").value("T1"))
                .andExpect(jsonPath("$[1].tradeId.tradeId").value("T2"));
    }

    @Test
    void getTradeById_ExistingTrade_ReturnsTrade() throws Exception {
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
        when(tradeService.getTradeById("T1")).thenReturn(Optional.of(trade));

        // Act & Assert
        mockMvc.perform(get("/api/trades/T1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tradeId.tradeId").value("T1"))
                .andExpect(jsonPath("$.tradeId.version").value(1));
    }

    @Test
    void getTradeById_NonExistingTrade_ReturnsNotFound() throws Exception {
        // Arrange
        when(tradeService.getTradeById("T1")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/trades/T1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTradesByTradeId_ReturnsListOfTrades() throws Exception {
        // Arrange
        TradeId tradeId1 = new TradeId("T1", 1);
        TradeId tradeId2 = new TradeId("T1", 2);
        List<Trade> trades = Arrays.asList(
                Trade.builder()
                        .tradeId(tradeId1)
                        .counterPartyId("CP-1")
                        .bookId("B1")
                        .maturityDate(LocalDate.now().plusDays(1))
                        .createdDate(LocalDate.now())
                        .expired(false)
                        .build(),
                Trade.builder()
                        .tradeId(tradeId2)
                        .counterPartyId("CP-1")
                        .bookId("B1")
                        .maturityDate(LocalDate.now().plusDays(1))
                        .createdDate(LocalDate.now())
                        .expired(false)
                        .build()
        );
        when(tradeService.getTradesByTradeId("T1")).thenReturn(trades);

        // Act & Assert
        mockMvc.perform(get("/api/trades/T1/versions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tradeId.version").value(1))
                .andExpect(jsonPath("$[1].tradeId.version").value(2));
    }
} 