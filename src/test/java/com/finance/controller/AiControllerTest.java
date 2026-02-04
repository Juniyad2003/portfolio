package com.finance.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;
import com.finance.service.GroqAiService;

@ExtendWith(MockitoExtension.class)
public class AiControllerTest {

    @Mock
    GroqAiService aiService;

    @Mock
    com.finance.service.PortfolioService portfolioService;

    @InjectMocks
    AiController aiController;

    private Portfolio samplePortfolio;

    @BeforeEach
    void setUp() {
        samplePortfolio = new Portfolio();
        samplePortfolio.setInvestmentGoal("LONG_TERM");
        samplePortfolio.setRiskPreference("MEDIUM");

        Asset a = new Asset("ABC", "STOCK", 100.0, 150.0, "MEDIUM", 10.0);
        a.setQuantity(5.0);
        samplePortfolio.setAssets(Collections.singletonList(a));
    }

    @Test
    void testChatForwardsToAiService() {
        when(aiService.askAi("hello"))
                .thenReturn("AI reply");

        String res = aiController.chat("hello");

        assertEquals("AI reply", res);
        verify(aiService).askAi("hello");
    }

    @Test
    void testAnalyzePortfolioHappyPath() throws Exception {
        when(portfolioService.findPortfolioById(1)).thenReturn(samplePortfolio);
        when(aiService.askAi(anyString())).thenReturn("Analysis result");

        String res = aiController.analyzePortfolio(1);

        assertEquals("Analysis result", res);
        verify(portfolioService).findPortfolioById(1);
        verify(aiService).askAi(anyString());
    }

    @Test
    void testAnalyzePortfolioEmptyPortfolio() throws Exception {
        Portfolio empty = new Portfolio();
        empty.setAssets(null);
        when(portfolioService.findPortfolioById(2)).thenReturn(empty);

        String res = aiController.analyzePortfolio(2);

        assertEquals("Portfolio is empty. Add assets to analyze.", res);
    }

    @Test
    void testAnalyzePortfolioServiceThrows() throws Exception {
        when(portfolioService.findPortfolioById(3)).thenThrow(new RuntimeException("db down"));

        String res = aiController.analyzePortfolio(3);

        assertTrue(res.startsWith("Error:"));
        assertTrue(res.contains("db down"));
    }
}
