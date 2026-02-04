package com.finance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class YahooFinanceService {

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    @Value("${rapidapi.host}")
    private String rapidApiHost;

    private final HttpClient httpClient;

    public YahooFinanceService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String getStock(String symbol) throws IOException, InterruptedException {
        /*
         * try {
         * HttpRequest request = HttpRequest.newBuilder()
         * .uri(URI.create("https://" + rapidApiHost +
         * "/api/v1/markets/stock/quotes?ticker=" + symbol))
         * .header("X-RapidAPI-Key", rapidApiKey)
         * .header("X-RapidAPI-Host", rapidApiHost)
         * .method("GET", HttpRequest.BodyPublishers.noBody())
         * .build();
         *
         * HttpResponse<String> response = httpClient.send(request,
         * HttpResponse.BodyHandlers.ofString());
         *
         * if (response.statusCode() != 200) {
         * System.err.println("RapidAPI error: " + response.statusCode() + " - " +
         * response.body());
         * return getMockStockData(symbol);
         * }
         *
         * return response.body();
         * } catch (Exception e) {
         * System.err.println("Failed to fetch stock from API: " + symbol + " - " +
         * e.getMessage());
         * return getMockStockData(symbol);
         * }
         */
        System.out.println("Fetching stock data (ALWAYS MOCK): " + symbol);
        return getMockStockData(symbol);
    }

    private String getMockStockData(String symbol) {
        double price = 100 + new java.util.Random().nextDouble() * 200;
        double change = -2.0 + new java.util.Random().nextDouble() * 4.0;
        double changePercent = (change / price) * 100;

        return String.format(java.util.Locale.US,
                "{\"body\":[{\"symbol\":\"%s\",\"shortName\":\"%s (Mock)\",\"regularMarketPrice\":%.2f,\"regularMarketChange\":%.2f,\"regularMarketChangePercent\":%.2f,\"regularMarketPreviousClose\":%.2f,\"regularMarketOpen\":%.2f,\"regularMarketDayLow\":%.2f,\"regularMarketDayHigh\":%.2f,\"regularMarketVolume\":1000000,\"marketCap\":1000000000,\"fiftyTwoWeekLow\":%.2f,\"fiftyTwoWeekHigh\":%.2f}]}",
                symbol, symbol, price, change, changePercent, price - change, price - (change / 2), price - 2,
                price + 2,
                price * 0.8, price * 1.2);
    }
}
