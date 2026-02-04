package com.finance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finance.entity.Portfolio;
import com.finance.entity.Asset;
import com.finance.exception.InvalidPortfolioIdException;
import com.finance.service.PortfolioService;
import com.finance.service.AssetService;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    PortfolioService portfolioService;
    AssetService assetService;

    public PortfolioController(PortfolioService portfolioService, AssetService assetService) {
        this.portfolioService = portfolioService;
        this.assetService = assetService;
    }

    @GetMapping("")
    public ResponseEntity<List<Portfolio>> getAllPortfolios() {
        List<Portfolio> portfolios = portfolioService.findAllPortfolios();

        // Recalculate portfolio values from assets to ensure accuracy
        for (Portfolio portfolio : portfolios) {
            List<Asset> assets = assetService.findAllAssetsByPortfolioId(portfolio.getId());
            com.finance.logic.PortfolioCalculator.calculatePortfolio(portfolio, assets);
            // Save the recalculated values to database
            try {
                portfolioService.updatePortfolio(portfolio);
            } catch (Exception e) {
                System.err.println("Failed to update portfolio " + portfolio.getId() + ": " + e.getMessage());
            }
        }

        return new ResponseEntity<>(portfolios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolioById(@PathVariable int id)
            throws InvalidPortfolioIdException {

        Portfolio portfolio = portfolioService.findPortfolioById(id);
        return new ResponseEntity<>(portfolio, HttpStatus.OK);
    }

    @GetMapping("/{id}/assets")
    public ResponseEntity<org.springframework.data.domain.Page<Asset>> getAssetsByPortfolio(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws InvalidPortfolioIdException {

        // Check if portfolio exists (optional, but good practice)
        portfolioService.findPortfolioById(id);

        org.springframework.data.domain.Page<Asset> assets = assetService.findAssetsByPortfolioId(id,
                org.springframework.data.domain.PageRequest.of(page, size));

        return new ResponseEntity<>(assets, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Portfolio> savePortfolio(@RequestBody Portfolio portfolio) {
        Portfolio saved = portfolioService.savePortfolio(portfolio);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(
            @PathVariable int id,
            @RequestBody Portfolio portfolio)
            throws InvalidPortfolioIdException {

        if (id != portfolio.getId()) {
            throw new InvalidPortfolioIdException(
                    "Portfolio id " + id + " is not matching with " + portfolio.getId());
        }

        Portfolio updated = portfolioService.updatePortfolio(portfolio);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Portfolio> deletePortfolio(@PathVariable int id)
            throws InvalidPortfolioIdException {

        Portfolio deleted = portfolioService.deletePortfolio(id);
        return new ResponseEntity<>(deleted, HttpStatus.OK);
    }

    @GetMapping("/{id}/performance")
    public ResponseEntity<List<com.finance.dto.PerformancePoint>> getPortfolioPerformance(
            @PathVariable int id,
            @RequestParam(defaultValue = "1M") String range)
            throws InvalidPortfolioIdException {

        // Check if portfolio exists
        Portfolio portfolio = portfolioService.findPortfolioById(id);

        // Simulate history
        List<com.finance.dto.PerformancePoint> history = new java.util.ArrayList<>();
        double currentValue = portfolio.getCurrentValue();

        // Volatility based on risk
        double volatility = switch (portfolio.getRiskPreference()) {
            case "HIGH" -> 0.05; // 5% daily swing
            case "MEDIUM" -> 0.02; // 2%
            case "LOW" -> 0.005; // 0.5%
            default -> 0.01;
        };

        int days = switch (range) {
            case "1W" -> 7;
            case "1Y" -> 365;
            default -> 30; // 1M
        };

        // Work backwards from today
        java.time.LocalDate date = java.time.LocalDate.now();
        history.add(new com.finance.dto.PerformancePoint(date, currentValue));

        for (int i = 1; i < days; i++) {
            date = date.minusDays(1);
            // Random change: -volatility to +volatility
            double change = (Math.random() * volatility * 2) - volatility;
            double prevValue = currentValue / (1 + change);

            history.add(new com.finance.dto.PerformancePoint(date, prevValue));
            currentValue = prevValue;
        }

        // Sort by date ascending for the graph
        history.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));

        return new ResponseEntity<>(history, HttpStatus.OK);
    }
}