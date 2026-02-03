package com.finance.service;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;
import com.finance.repo.AssetRepo;
import com.finance.repo.PortfolioRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GroqAiService {

    @Value("${groq.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askAi(String question) {

        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String body = """
        {
          "model": "llama-3.1-8b-instant",
          "messages": [
            {
              "role": "system",
              "content": "You are a financial education assistant. Do not give investment advice or price prediction."
            },
            {
              "role": "user",
              "content": "%s"
            }
          ]
        }
        """.formatted(
                question
                        .replace("\\", "\\\\")
                        .replace("\"", "")
                        .replace("\n", "\\n")
                        .replace("\r", "")
        );

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
            return "Groq API error: " + e.getMessage();
        }
    }

    @Autowired
    public PortfolioRepo portfolioRepo;

    @Autowired
    public AssetRepo assetRepo;

    public String analyzePortfolioFromDb(int portfolioId) {

        Portfolio portfolio = portfolioRepo.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        List<Asset> assets = assetRepo.findAllByPortfolio(portfolio);

        StringBuilder prompt = new StringBuilder();

        prompt.append("Analyze the following investment portfolio:\n");
        prompt.append("Investor name: ").append(portfolio.getName()).append("\n");
        prompt.append("Goal: ").append(portfolio.getInvestmentGoal()).append("\n");
        prompt.append("Risk preference: ").append(portfolio.getRiskPreference()).append("\n");
        prompt.append("Total investment: ").append(portfolio.getTotalInvestment()).append("\n");
        prompt.append("Current value: ").append(portfolio.getCurrentValue()).append("\n");
        prompt.append("Profit/Loss: ")
                .append(portfolio.getCurrentValue() - portfolio.getTotalInvestment())
                .append("\n\n");

        prompt.append("Assets:\n");

        for (Asset a : assets) {
            prompt.append(
                    "Asset: " + a.getAssetName() +
                            ", Type: " + a.getAssetType() +
                            ", Invested: " + a.getInvestedAmount() +
                            ", Current: " + a.getCurrentAmount() + "\n"
            );
        }

        prompt.append(
                "\nGive risk analysis, diversification quality, and improvement suggestions. Do not give investment advice."
        );

        return askAi(prompt.toString());
    }


}
