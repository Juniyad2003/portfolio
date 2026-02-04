package com.finance.controller;

import com.finance.service.YahooFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

    private final YahooFinanceService yahooFinanceService;

    @Autowired
    public MarketDataController(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    @GetMapping(value = "/{symbol}", produces = "application/json")
    public ResponseEntity<?> getStockData(@PathVariable String symbol) {
        try {
            String jsonResponse = yahooFinanceService.getStock(symbol);
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("{\"error\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
