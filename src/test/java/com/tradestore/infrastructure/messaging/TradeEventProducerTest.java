package com.tradestore.infrastructure.messaging;

import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class TradeEventProducerTest {

    @MockBean
    private KafkaTemplate<String, Trade> kafkaTemplate;

    @Autowired
    private TradeEventProducer tradeEventProducer;

    @Test
    void shouldSendTradeEvent() {
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
        tradeEventProducer.sendTradeEvent(trade);

        // Assert
        verify(kafkaTemplate).send(eq("trades"), any(String.class), eq(trade));
    }
} 