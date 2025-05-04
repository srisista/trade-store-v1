package com.tradestore.infrastructure.messaging;

import com.tradestore.config.KafkaTestConfig;
import com.tradestore.domain.model.Trade;
import com.tradestore.domain.model.TradeId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ContextConfiguration(classes = {KafkaTestConfig.class})
class TradeEventProducerTest {

    @Autowired
    private KafkaTemplate<String, Trade> kafkaTemplate;

    @Autowired
    private TradeEventProducer tradeEventProducer;

    @Test
    void shouldSendTradeEvent() {
        // Given
        Trade trade = Trade.builder()
                .tradeId(new TradeId(UUID.randomUUID().toString(), 1))
                .counterPartyId("CP-1")
                .bookId("B1")
                .maturityDate(LocalDate.now().plusDays(1))
                .createdDate(LocalDate.now())
                .expired(false)
                .build();

        // When/Then
        assertDoesNotThrow(() -> tradeEventProducer.sendTradeEvent(trade));
    }
} 