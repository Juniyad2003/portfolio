package com.finance.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
//@OneToMany(mappedBy = "asset", cascade = CascadeType.ALL)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    String assetName;
    String assetType;   // STOCK, MUTUAL_FUND, CRYPTO, ETF

    double investedAmount;
    double currentAmount;

    String volatilityLevel;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    Portfolio portfolio;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL)
    List<Transaction> transactions;


    public Asset() {
        super();
    }

    public Asset(String assetName, String assetType,
                 double investedAmount, double currentAmount,
                 String volatilityLevel) {

        super();
        this.assetName = assetName;
        this.assetType = assetType;
        this.investedAmount = investedAmount;
        this.currentAmount = currentAmount;
        this.volatilityLevel = volatilityLevel;
    }

    public int getId() {
        return id;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public double getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(double investedAmount) {
        this.investedAmount = investedAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public String getVolatilityLevel() {
        return volatilityLevel;
    }

    public void setVolatilityLevel(String volatilityLevel) {
        this.volatilityLevel = volatilityLevel;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }


    @Override
    public String toString() {
        return "Asset [id=" + id + ", assetName=" + assetName +
                ", assetType=" + assetType +
                ", investedAmount=" + investedAmount +
                ", currentAmount=" + currentAmount +
                ", volatilityLevel=" + volatilityLevel + "]";
    }
}
