package com.finance.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.finance.service.YahooFinanceService;

@ExtendWith(MockitoExtension.class)
public class MarketDataControllerTest {

    @Mock
    private YahooFinanceService yahooFinanceService;

    @InjectMocks
    private MarketDataController marketDataController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetStockDataSuccess() throws Exception {
        String symbol = "AAPL";
        String mockJson = "{\"symbol\": \"AAPL\", \"price\": 150.0}";

        when(yahooFinanceService.getStock(symbol)).thenReturn(mockJson);

        ResponseEntity<?> resp = marketDataController.getStockData(symbol);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(mockJson, resp.getBody());
        verify(yahooFinanceService).getStock(symbol);
    }

    @Test
    void testGetStockDataException() throws Exception {
        String symbol = "FAIL";
        when(yahooFinanceService.getStock(symbol)).thenThrow(new RuntimeException("api down"));

        ResponseEntity<?> resp = marketDataController.getStockData(symbol);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("{\"error\": \"api down\"}", resp.getBody());
        verify(yahooFinanceService).getStock(symbol);
    }
}
