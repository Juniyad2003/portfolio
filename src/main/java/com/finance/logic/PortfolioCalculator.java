package com.finance.logic;

import java.util.List;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;

public class PortfolioCalculator {

    public static void calculatePortfolio(Portfolio portfolio, List<Asset> assets) {

        double totalInvestment = 0;
        double currentValue = 0;

        System.out.println(
                "=== Calculating Portfolio: " + portfolio.getPortfolioName() + " (ID: " + portfolio.getId() + ") ===");
        System.out.println("Total assets to calculate: " + assets.size());

        for (Asset asset : assets) {
            System.out.println("  Asset: " + asset.getAssetName() +
                    " | Type: " + asset.getAssetType() +
                    " | Invested: $" + asset.getInvestedAmount() +
                    " | Current: $" + asset.getCurrentAmount());
            totalInvestment += asset.getInvestedAmount();
            currentValue += asset.getCurrentAmount();
        }

        System.out.println("Total Investment: $" + totalInvestment);
        System.out.println("Current Value: $" + currentValue);
        System.out.println("Profit/Loss: $" + (currentValue - totalInvestment));
        System.out.println("==============================================");

        portfolio.setTotalInvestment(totalInvestment);
        portfolio.setCurrentValue(currentValue);
        portfolio.setTotalProfitLoss(currentValue - totalInvestment);
    }
}
