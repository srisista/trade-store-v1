package com.tradestore.infrastructure.messaging;

import com.tradestore.domain.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEventProducer {
    private final KafkaTemplate<String, Trade> kafkaTemplate;
    private static final String TOPIC = "trades";

    public void sendTradeEvent(Trade trade) {
        CompletableFuture<SendResult<String, Trade>> future = 
            kafkaTemplate.send(TOPIC, trade.getTradeId().toString(), trade);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Sent trade event for trade ID: {} to topic: {}", 
                    trade.getTradeId(), TOPIC);
            } else {
                log.error("Failed to send trade event for trade ID: {} to topic: {}", 
                    trade.getTradeId(), TOPIC, ex);
            }
        });
    }
} 