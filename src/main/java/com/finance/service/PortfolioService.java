package com.finance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finance.entity.Portfolio;
import com.finance.exception.InvalidPortfolioIdException;
import com.finance.repo.PortfolioRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);
    private PortfolioRepo portfolioRepo;
    private com.finance.repo.AssetRepo assetRepo;
    private com.finance.repo.TransactionRepo transactionRepo;

    @Autowired
    public PortfolioService(PortfolioRepo portfolioRepo, com.finance.repo.AssetRepo assetRepo,
                            com.finance.repo.TransactionRepo transactionRepo) {
        this.portfolioRepo = portfolioRepo;
        this.assetRepo = assetRepo;
        this.transactionRepo = transactionRepo;
    }

    public Portfolio savePortfolio(Portfolio portfolio) {
        return portfolioRepo.save(portfolio);
    }

    public Portfolio findPortfolioById(int id) throws InvalidPortfolioIdException {
        log.info("Finding Portfolio | id ={}",id);
        Optional<Portfolio> opt = portfolioRepo.findById(id);
        if (opt.isEmpty()) {
            throw new InvalidPortfolioIdException("Portfolio ID " + id + " is not valid");
        }
        return opt.get();
    }

    public List<Portfolio> findAllPortfolios() {
        return portfolioRepo.findAll();
    }

    public Portfolio updatePortfolio(Portfolio portfolio) throws InvalidPortfolioIdException {
        findPortfolioById(portfolio.getId());
        return portfolioRepo.save(portfolio);
    }

    public Portfolio deletePortfolio(int id) throws InvalidPortfolioIdException {
        Portfolio portfolio = findPortfolioById(id);
        portfolioRepo.deleteById(id);
        return portfolio;
    }

    public void buyStock(int portfolioId, String symbol, double quantity, double price, String assetType)
            throws InvalidPortfolioIdException {
        log.info("BUY | portfolio={},symbol={},qty={}",portfolioId,symbol,quantity);
        System.out.println("Searching for portfolio ID: " + portfolioId);
        System.out.println("Asset Type received: " + assetType);
        Portfolio portfolio = findPortfolioById(portfolioId);
        System.out.println("Found portfolio: " + portfolio.getPortfolioName());

        // Check if asset exists
        com.finance.entity.Asset asset = null;
        if (portfolio.getAssets() != null) {
            asset = portfolio.getAssets().stream()
                    .filter(a -> symbol.equals(a.getAssetName()))
                    .findFirst()
                    .orElse(null);
        }

        if (asset == null) {
            String finalAssetType = assetType != null ? assetType : "STOCK";
            System.out.println(
                    "No existing asset found for " + symbol + ". Creating new asset with type: " + finalAssetType);
            asset = new com.finance.entity.Asset(symbol, finalAssetType, 0, 0, "MEDIUM", 0);
            asset.setPortfolio(portfolio);
        } else {
            System.out.println("Existing asset found for " + symbol + ". Quantity: " + asset.getQuantity());
        }

        double totalCost = quantity * price;

        // Update Asset
        asset.setQuantity(asset.getQuantity() + quantity);
        asset.setInvestedAmount(asset.getInvestedAmount() + totalCost);
        asset.setCurrentAmount(asset.getQuantity() * price); // Update current value based on latest price

        asset = assetRepo.save(asset);
        System.out.println("Asset updated/saved. New quantity: " + asset.getQuantity());

        // Create Transaction
        com.finance.entity.Transaction transaction = new com.finance.entity.Transaction();
        transaction.setTransactionType("BUY");
        transaction.setQuantity(quantity);
        transaction.setAmount(totalCost);
        transaction.setAsset(asset);
        transaction.setTransactionDate(java.time.LocalDateTime.now());
        // Note: Transaction inherits Investor fields which is messy, ignoring them for
        // now.

        transactionRepo.save(transaction);
        System.out.println("Transaction saved.");

        // Update Portfolio
        portfolio.setTotalInvestment(portfolio.getTotalInvestment() + totalCost);
        portfolio.setCurrentValue(portfolio.getCurrentValue() + totalCost); // Simplified
        portfolioRepo.save(portfolio);
        System.out.println("Portfolio " + portfolioId + " updated.");
    }

    public void sellStock(int portfolioId, String symbol, double quantity, double price, String assetType)
            throws InvalidPortfolioIdException {
        log.info("SELL | portfolio={},symbol={},qty={}",portfolioId,symbol,quantity);

        System.out.println("Searching for portfolio ID: " + portfolioId + " to SELL " + symbol);
        Portfolio portfolio = findPortfolioById(portfolioId);

        com.finance.entity.Asset asset = null;
        if (portfolio.getAssets() != null) {
            asset = portfolio.getAssets().stream()
                    .filter(a -> symbol.equals(a.getAssetName()))
                    .findFirst()
                    .orElse(null);
        }

        if (asset == null || asset.getQuantity() < quantity) {
            System.err.println("Insufficient assets. Asset: " + (asset != null ? asset.getAssetName() : "NULL")
                    + ", Available: " + (asset != null ? asset.getQuantity() : 0) + ", Requested: " + quantity);
            throw new RuntimeException("Insufficient assets to sell");
        }

        double totalSale = quantity * price;

        // Update Asset
        double fraction = quantity / (asset.getQuantity());
        double investedRemoved = asset.getInvestedAmount() * fraction;

        asset.setQuantity(asset.getQuantity() - quantity);
        asset.setInvestedAmount(asset.getInvestedAmount() - investedRemoved);
        asset.setCurrentAmount(asset.getQuantity() * price);

        assetRepo.save(asset);
        System.out.println("Asset updated/saved after SELL. New quantity: " + asset.getQuantity());

        // Create Transaction
        com.finance.entity.Transaction transaction = new com.finance.entity.Transaction();
        transaction.setTransactionType("SELL");
        transaction.setQuantity(quantity);
        transaction.setAmount(totalSale);
        transaction.setAsset(asset);
        transaction.setTransactionDate(java.time.LocalDateTime.now());

        transactionRepo.save(transaction);
        System.out.println("SELL Transaction saved.");

        // Update Portfolio
        portfolio.setTotalInvestment(portfolio.getTotalInvestment() - investedRemoved);
        portfolio.setCurrentValue(portfolio.getCurrentValue() - (quantity * price)); // Approx simple update
        double profit = totalSale - investedRemoved;
        portfolio.setTotalProfitLoss(portfolio.getTotalProfitLoss() + profit);

        portfolioRepo.save(portfolio);
        System.out.println("Portfolio " + portfolioId + " updated after SELL.");
    }
}