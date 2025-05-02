package com.tradestore.api.exception;

import com.tradestore.domain.exception.TradeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("test-path");
    }

    @Test
    void handleTradeException_ShouldReturnBadRequest() {
        // Arrange
        String message = "Invalid trade";
        TradeException exception = new TradeException(message);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleTradeException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().get("message"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("test-path", response.getBody().get("path"));
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerError() {
        // Arrange
        String message = "Unexpected error";
        Exception exception = new Exception(message);

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("test-path", response.getBody().get("path"));
    }
} 