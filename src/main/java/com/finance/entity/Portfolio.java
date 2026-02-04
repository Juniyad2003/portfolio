package com.finance.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class Portfolio extends Investor {

    String portfolioName;
    double totalInvestment;
    double currentValue;
    double totalProfitLoss;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    List<Asset> assets;

    public Portfolio() {
        super();
    }

    public Portfolio(int id, String name, String email,
                     String investmentGoal, String riskPreference,
                     String portfolioName) {

        super(id, name, email, investmentGoal, riskPreference, null);
        this.portfolioName = portfolioName;
    }

    public Portfolio(String name, String email,
                     String investmentGoal, String riskPreference,
                     String portfolioName) {

        super(name, email, investmentGoal, riskPreference, null);
        this.portfolioName = portfolioName;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public double getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(double totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public void setTotalProfitLoss(double totalProfitLoss) {
        this.totalProfitLoss = totalProfitLoss;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    @Override
    public String toString() {
        return "Portfolio [ " + super.toString() +
                ", portfolioName=" + portfolioName +
                ", totalInvestment=" + totalInvestment +
                ", currentValue=" + currentValue +
                ", totalProfitLoss=" + totalProfitLoss + "]";
    }
}