package com.finance.controller;

import com.finance.service.GroqAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    GroqAiService aiService;

    @Autowired
    com.finance.service.PortfolioService portfolioService;

    @PostMapping(value = "/chat", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String chat(@RequestBody String question) {
        return aiService.askAi(question);
    }

    @GetMapping("/analyze-portfolio/{portfolioId}")
    public String analyzePortfolio(@PathVariable int portfolioId) {
        try {
            com.finance.entity.Portfolio portfolio = portfolioService.findPortfolioById(portfolioId);
            StringBuilder prompt = new StringBuilder(
                    "Analyze this investment portfolio. Provide a short risk assessment, diversification check, and specific suggestions. Do not use markdown formatting like bold/italic, just plain text with newlines.\n");
            prompt.append("Context: User Goal=").append(portfolio.getInvestmentGoal())
                    .append(", Risk Pref=").append(portfolio.getRiskPreference()).append("\n");
            prompt.append("Holdings:\n");

            if (portfolio.getAssets() != null) {
                for (com.finance.entity.Asset asset : portfolio.getAssets()) {
                    if (asset.getQuantity() > 0) {
                        double avgPrice = asset.getInvestedAmount() / asset.getQuantity();
                        prompt.append("- ").append(asset.getAssetName())
                                .append(": ").append(asset.getQuantity()).append(" shares @ $")
                                .append(String.format("%.2f", avgPrice)).append("\n");
                    }
                }
            } else {
                return "Portfolio is empty. Add assets to analyze.";
            }

            return aiService.askAi(prompt.toString());
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}