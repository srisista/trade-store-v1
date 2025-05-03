package com.tradestore.infrastructure.scheduler;

import com.tradestore.domain.service.TradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeExpirationSchedulerTest {

    @Mock
    private TradeService tradeService;

    @Mock
    private Logger logger;

    @InjectMocks
    private TradeExpirationScheduler scheduler;

    private MockedStatic<LoggerFactory> mockedStatic;

    @BeforeEach
    void setUp() {
        mockedStatic = mockStatic(LoggerFactory.class);
        mockedStatic.when(() -> LoggerFactory.getLogger(TradeExpirationScheduler.class))
            .thenReturn(logger);
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    @Test
    void updateExpiredTrades_WhenServiceSucceeds_ShouldNotThrowException() {
        // Arrange
        doNothing().when(tradeService).updateExpiredTrades();

        // Act
        scheduler.updateExpiredTrades();

        // Assert
        verify(tradeService, times(1)).updateExpiredTrades();
        verify(logger, never()).error(anyString(), any(Object.class), any(Throwable.class));
    }

    @Test
    void updateExpiredTrades_WhenServiceThrowsException_ShouldLogErrorAndNotPropagate() {
        // Arrange
        RuntimeException exception = new RuntimeException("Test exception");
        doThrow(exception).when(tradeService).updateExpiredTrades();

        // Act
        scheduler.updateExpiredTrades();

        // Assert
        verify(tradeService, times(1)).updateExpiredTrades();
        verify(logger, times(1)).error(eq("Error updating expired trades: {}"), eq(exception.getMessage()), eq(exception));
    }

} 