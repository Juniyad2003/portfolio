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
        return new ResponseEntity<>(portfolios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolioById(@PathVariable int id)
            throws InvalidPortfolioIdException {

        Portfolio portfolio = portfolioService.findPortfolioById(id);
        return new ResponseEntity<>(portfolio, HttpStatus.OK);
    }

    @GetMapping("/{id}/assets")
    public ResponseEntity<List<Asset>> getAssetsByPortfolio(@PathVariable int id)
            throws InvalidPortfolioIdException {

        Portfolio portfolio = portfolioService.findPortfolioById(id);
        List<Asset> assets = assetService.findAssetsByPortfolio(portfolio);

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
}
