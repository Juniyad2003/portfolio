package com.finance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.finance.entity.Portfolio;
import com.finance.exception.InvalidPortfolioIdException;
import com.finance.repo.PortfolioRepo;

@Service
public class PortfolioService {

    private PortfolioRepo portfolioRepo;

    public PortfolioService(PortfolioRepo portfolioRepo) {
        this.portfolioRepo = portfolioRepo;
    }

    public Portfolio savePortfolio(Portfolio portfolio) {
        return portfolioRepo.save(portfolio);
    }

    public Portfolio findPortfolioById(int id) throws InvalidPortfolioIdException {
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
}
