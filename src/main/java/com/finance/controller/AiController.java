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

    // Raw chat
    @PostMapping(value = "/chat", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String chat(@RequestBody String question) {
        return aiService.askAi(question);
    }


    // Portfolio analysis from DB
    @GetMapping("/portfolio/{portfolioId}")
    public String analyzePortfolio(@PathVariable int portfolioId) {
        return aiService.analyzePortfolioFromDb(portfolioId);
    }


}


