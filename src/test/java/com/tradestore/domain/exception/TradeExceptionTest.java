package com.tradestore.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TradeExceptionTest {

    @Test
    void constructor_WithMessage_CreatesExceptionWithMessage() {
        // Arrange
        String message = "Invalid trade";

        // Act
        TradeException exception = new TradeException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithMessageAndCause_CreatesExceptionWithMessageAndCause() {
        // Arrange
        String message = "Invalid trade";
        Throwable cause = new RuntimeException("Root cause");

        // Act
        TradeException exception = new TradeException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
} 