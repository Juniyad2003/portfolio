package com.finance.entity;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction extends Investor {

    String transactionType; // BUY / SELL
    double quantity;
    double amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime transactionDate;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    Asset asset;

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Asset getAsset() {
        return asset;
    }

    public Transaction() {
        super();
        this.transactionDate = LocalDateTime.now();
    }

    public Transaction(int id, String name, String email,
                       String investmentGoal, String riskPreference,
                       String transactionType, double quantity, double amount) {

        super(id, name, email, investmentGoal, riskPreference, null);
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.amount = amount;
        this.transactionDate = LocalDateTime.now();
    }

    public Transaction(String name, String email,
                       String investmentGoal, String riskPreference,
                       String transactionType, double quantity, double amount) {

        super(name, email, investmentGoal, riskPreference, null);
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.amount = amount;
        this.transactionDate = LocalDateTime.now();
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDisplayDate() {
        return transactionDate != null ? transactionDate.toString() : "";
    }

    @Override
    public String toString() {
        return "Transaction [ " + super.toString() +
                ", transactionType=" + transactionType +
                ", quantity=" + quantity +
                ", amount=" + amount +
                ", transactionDate=" + transactionDate + "]";
    }
}