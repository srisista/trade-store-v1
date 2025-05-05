package com.tradestore.application.service;

import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import com.tradestore.infrastructure.repository.TradeRepository;
import com.tradestore.infrastructure.repository.TradeJpaRepository;
import com.tradestore.domain.service.TradeService;
import com.tradestore.domain.service.impl.TradeServiceImpl;
import com.tradestore.infrastructure.mapper.TradeMapper;
import com.tradestore.infrastructure.entity.TradeEntity;
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
    private TradeRepository mongoRepository;

    @Mock
    private TradeJpaRepository jpaRepository;

    @Mock
    private TradeMapper tradeMapper;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private Trade trade;
    private TradeId tradeId;
    private TradeEntity tradeEntity;

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

        tradeEntity = TradeEntity.builder()
                .id(1L)
                .tradeId(tradeId.getTradeId())
                .version(tradeId.getVersion())
                .counterPartyId(trade.getCounterPartyId())
                .bookId(trade.getBookId())
                .maturityDate(trade.getMaturityDate())
                .createdDate(trade.getCreatedDate())
                .expired(trade.isExpired())
                .build();
    }

    @Test
    void storeTrade() {
        when(mongoRepository.findByTradeIdAndVersion(tradeId.getTradeId(), tradeId.getVersion()))
            .thenReturn(Optional.empty());
        when(mongoRepository.save(any(Trade.class))).thenReturn(trade);
        when(tradeMapper.toEntity(any(Trade.class))).thenReturn(tradeEntity);
        when(jpaRepository.save(any(TradeEntity.class))).thenReturn(tradeEntity);
        
        Trade savedTrade = tradeService.storeTrade(trade);
        assertNotNull(savedTrade);
        assertEquals(tradeId.getTradeId(), savedTrade.getTradeId().getTradeId());
        assertEquals(tradeId.getVersion(), savedTrade.getTradeId().getVersion());
        
        verify(mongoRepository).findByTradeIdAndVersion(tradeId.getTradeId(), tradeId.getVersion());
        verify(mongoRepository).save(trade);
        verify(tradeMapper).toEntity(trade);
        verify(jpaRepository).save(tradeEntity);
    }

    @Test
    void getTradeById() {
        when(mongoRepository.findByTradeIdAndVersion(tradeId.getTradeId(), tradeId.getVersion()))
            .thenReturn(Optional.of(trade));
        
        Optional<Trade> retrievedTrade = tradeService.getTradeById(tradeId.getTradeId(), tradeId.getVersion());
        assertTrue(retrievedTrade.isPresent());
        assertEquals(tradeId.getTradeId(), retrievedTrade.get().getTradeId().getTradeId());
        assertEquals(tradeId.getVersion(), retrievedTrade.get().getTradeId().getVersion());
        
        verify(mongoRepository).findByTradeIdAndVersion(tradeId.getTradeId(), tradeId.getVersion());
    }

    @Test
    void getTradesByTradeId() {
        Trade tradeV2 = Trade.builder()
                .tradeId(new TradeId("T1", 2))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        when(mongoRepository.findByTradeIdOrderByVersionDesc("T1"))
            .thenReturn(Arrays.asList(trade, tradeV2));

        List<Trade> versions = tradeService.getTradesByTradeId("T1");
        assertEquals(2, versions.size());
        
        verify(mongoRepository).findByTradeIdOrderByVersionDesc("T1");
    }

    @Test
    void updateExpiredTrades() {
        Trade expiredTrade = Trade.builder()
                .tradeId(new TradeId("T2", 1))
                .counterPartyId("CP-2")
                .bookId("B2")
                .maturityDate(LocalDate.now().minusDays(1))
                .createdDate(LocalDate.now().minusDays(2))
                .expired(false)
                .build();

        TradeEntity expiredTradeEntity = TradeEntity.builder()
                .id(2L)
                .tradeId("T2")
                .version(1)
                .counterPartyId("CP-2")
                .bookId("B2")
                .maturityDate(LocalDate.now().minusDays(1))
                .createdDate(LocalDate.now().minusDays(2))
                .expired(false)
                .build();

        when(mongoRepository.findByMaturityDateBeforeAndExpiredFalse(any(LocalDate.class)))
            .thenReturn(Arrays.asList(expiredTrade));
        when(mongoRepository.save(any(Trade.class))).thenReturn(expiredTrade);
        when(tradeMapper.toEntity(any(Trade.class))).thenReturn(expiredTradeEntity);
        when(jpaRepository.save(any(TradeEntity.class))).thenReturn(expiredTradeEntity);
        when(mongoRepository.findAll())
            .thenReturn(Arrays.asList(trade, expiredTrade));

        tradeService.updateExpiredTrades();

        List<Trade> allTrades = tradeService.getAllTrades();
        assertEquals(2, allTrades.size());
        
        verify(mongoRepository).findByMaturityDateBeforeAndExpiredFalse(any(LocalDate.class));
        verify(mongoRepository).save(expiredTrade);
        verify(tradeMapper).toEntity(expiredTrade);
        verify(jpaRepository).save(expiredTradeEntity);
        verify(mongoRepository).findAll();
    }
} 