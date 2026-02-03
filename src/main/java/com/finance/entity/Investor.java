package com.finance.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Investor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    String name;
    String email;

    String investmentGoal;   // SHORT_TERM, LONG_TERM, RETIREMENT
    String riskPreference;   // LOW, MEDIUM, HIGH

    LocalDateTime createdAt;

    public Investor() {
        super();
    }

    public Investor(int id, String name, String email,
                    String investmentGoal, String riskPreference) {
        super();
        this.id = id;
        this.name = name;
        this.email = email;
        this.investmentGoal = investmentGoal;
        this.riskPreference = riskPreference;
        this.createdAt = LocalDateTime.now();
    }

    public Investor(String name, String email,
                    String investmentGoal, String riskPreference) {
        super();
        this.name = name;
        this.email = email;
        this.investmentGoal = investmentGoal;
        this.riskPreference = riskPreference;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInvestmentGoal() {
        return investmentGoal;
    }

    public void setInvestmentGoal(String investmentGoal) {
        this.investmentGoal = investmentGoal;
    }

    public String getRiskPreference() {
        return riskPreference;
    }

    public void setRiskPreference(String riskPreference) {
        this.riskPreference = riskPreference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Investor [id=" + id + ", name=" + name + ", email=" + email +
                ", investmentGoal=" + investmentGoal +
                ", riskPreference=" + riskPreference + "]";
    }


}
