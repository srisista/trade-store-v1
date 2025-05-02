package com.tradestore.domain.exception;

public class TradeException extends RuntimeException {
    public TradeException(String message) {
        super(message);
    }

    public TradeException(String message, Throwable cause) {
        super(message, cause);
    }
} 