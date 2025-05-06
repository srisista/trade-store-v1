package com.tradestore.infrastructure.messaging;

import com.tradestore.domain.model.Trade;
import com.tradestore.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class TradeEventProducerTest {

    @Mock
    private KafkaTemplate<String, Trade> kafkaTemplate;

    @InjectMocks
    private TradeEventProducer eventProducer;

    @Test
    void shouldSendTradeEvent() {
        // Arrange
        Trade trade = TestUtils.createValidTrade();
        when(kafkaTemplate.send(eq("trades"), eq(trade.getTradeId().toString()), eq(trade)))
            .thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

        // Act & Assert
        assertDoesNotThrow(() -> eventProducer.sendTradeEvent(trade));
    }

    @Test
    void shouldSendTradeEventWithExpiredTrade() {
        // Arrange
        Trade expiredTrade = TestUtils.createExpiredTrade();
        when(kafkaTemplate.send(eq("trades"), eq(expiredTrade.getTradeId().toString()), eq(expiredTrade)))
            .thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

        // Act & Assert
        assertDoesNotThrow(() -> eventProducer.sendTradeEvent(expiredTrade));
    }
} 