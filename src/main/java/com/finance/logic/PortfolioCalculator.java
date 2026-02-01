package com.finance.logic;

import java.util.List;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;

public class PortfolioCalculator {

    public static void calculatePortfolio(Portfolio portfolio, List<Asset> assets) {

        double totalInvestment = 0;
        double currentValue = 0;

        for (Asset asset : assets) {
            totalInvestment += asset.getInvestedAmount();
            currentValue += asset.getCurrentAmount();
        }

        portfolio.setTotalInvestment(totalInvestment);
        portfolio.setCurrentValue(currentValue);
        portfolio.setTotalProfitLoss(currentValue - totalInvestment);
    }
}
