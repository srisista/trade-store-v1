package com.tradestore.infrastructure.scheduler;

import com.tradestore.domain.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeExpirationSchedulerTest {

    @Mock
    private TradeService tradeService;

    @Mock
    private Logger logger;

    private TradeExpirationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TradeExpirationScheduler(tradeService);
        ReflectionTestUtils.setField(scheduler, "logger", logger);
    }

    @Test
    void updateExpiredTrades_WhenServiceThrowsException_ShouldLogErrorAndNotPropagate() {
        // Arrange
        RuntimeException testException = new RuntimeException("Test exception");
        doThrow(testException).when(tradeService).updateExpiredTrades();

        // Act
        scheduler.updateExpiredTrades();

        // Assert
        verify(logger).error(eq("Error updating expired trades: {}"), eq("Test exception"), eq(testException));
    }

    @Test
    void updateExpiredTrades_WhenServiceSucceeds_ShouldNotLogError() {
        // Act
        scheduler.updateExpiredTrades();

        // Assert
        verify(logger, never()).error(anyString(), any(), any());
    }
} 