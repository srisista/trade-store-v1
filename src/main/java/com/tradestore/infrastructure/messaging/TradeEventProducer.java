package com.tradestore.infrastructure.messaging;

import com.tradestore.domain.model.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeEventProducer {
    private final KafkaTemplate<String, Trade> kafkaTemplate;
    private static final String TOPIC = "trades";

    public void sendTradeEvent(Trade trade) {
        kafkaTemplate.send(TOPIC, trade.getTradeId().toString(), trade);
    }
} 