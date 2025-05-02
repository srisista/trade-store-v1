package com.tradestore.infrastructure.scheduler;

import com.tradestore.domain.service.TradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeExpirationSchedulerTest {

    @Mock
    private TradeService tradeService;

    @InjectMocks
    private TradeExpirationScheduler scheduler;

    @Test
    void updateExpiredTrades_ShouldCallService() {
        // Act
        scheduler.updateExpiredTrades();

        // Assert
        verify(tradeService, times(1)).updateExpiredTrades();
    }

    @Test
    void updateExpiredTrades_WhenServiceThrowsException_ShouldHandleGracefully() {
        // Arrange
        RuntimeException exception = new RuntimeException("Test exception");
        doThrow(exception).when(tradeService).updateExpiredTrades();

        // Act & Assert
        scheduler.updateExpiredTrades(); // Should not throw exception
        verify(tradeService, times(1)).updateExpiredTrades();
    }
} 