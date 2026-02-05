package com.finance.logic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;

public class PortfolioCalculatorTest {

    @Test
    void testCalculatePortfolio() {
        Portfolio p = new Portfolio();
        p.setId(1);
        p.setPortfolioName("TestPortfolio");

        Asset a1 = new Asset("A", "STOCK", 100.0, 120.0, "MEDIUM", 10.0);
        Asset a2 = new Asset("B", "ETF", 200.0, 180.0, "LOW", 5.0);

        PortfolioCalculator.calculatePortfolio(p, Arrays.asList(a1, a2));

        assertEquals(300.0, p.getTotalInvestment(), 0.0001);
        assertEquals(300.0, p.getCurrentValue(), 0.0001);
        assertEquals(0.0, p.getTotalProfitLoss(), 0.0001);
    }
}
