package com.finance.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    void testChatEmptyQuestion() {
        when(aiService.askAi("")).thenReturn("AI reply for empty");

        String res = aiController.chat("");

        assertEquals("AI reply for empty", res);
        verify(aiService).askAi("");
    }

    @Test
    void testChatServiceThrows() {
        when(aiService.askAi("boom")).thenThrow(new RuntimeException("ai down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aiController.chat("boom"));
        assertTrue(ex.getMessage().contains("ai down"));
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
    void testAnalyzePortfolioPromptFormatting() throws Exception {
        when(portfolioService.findPortfolioById(1)).thenReturn(samplePortfolio);
        when(aiService.askAi(anyString())).thenReturn("Analysis result");

        String res = aiController.analyzePortfolio(1);

        assertEquals("Analysis result", res);
        verify(portfolioService).findPortfolioById(1);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(aiService).askAi(captor.capture());
        String prompt = captor.getValue();

        assertTrue(prompt.contains("Context: User Goal=LONG_TERM"));
        assertTrue(prompt.contains("Risk Pref=MEDIUM"));
        assertTrue(prompt.contains("Holdings:\n"));
        assertTrue(prompt.contains("- ABC: 5.0 shares @ $20.00"));
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
    void testAnalyzePortfolioEmptyAssetsList() throws Exception {
        Portfolio p = new Portfolio();
        p.setInvestmentGoal("SHORT_TERM");
        p.setRiskPreference("LOW");
        p.setAssets(Collections.emptyList());

        when(portfolioService.findPortfolioById(4)).thenReturn(p);
        when(aiService.askAi(anyString())).thenReturn("No holdings analysis");

        String res = aiController.analyzePortfolio(4);

        assertEquals("No holdings analysis", res);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(aiService).askAi(captor.capture());
        String prompt = captor.getValue();

        assertTrue(prompt.contains("Holdings:\n"));
        assertFalse(prompt.contains("- "));
    }

    @Test
    void testAnalyzePortfolioAllZeroQuantityAssets() throws Exception {
        Asset z = new Asset("ZERO", "STOCK", 50.0, 60.0, "LOW", 0.0);
        z.setQuantity(0.0);
        Portfolio p = new Portfolio();
        p.setInvestmentGoal("SHORT_TERM");
        p.setRiskPreference("LOW");
        p.setAssets(Collections.singletonList(z));

        when(portfolioService.findPortfolioById(5)).thenReturn(p);
        when(aiService.askAi(anyString())).thenReturn("Analysis with no active shares");

        String res = aiController.analyzePortfolio(5);

        assertEquals("Analysis with no active shares", res);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(aiService).askAi(captor.capture());
        String prompt = captor.getValue();

        assertTrue(prompt.contains("Holdings:\n"));
        assertFalse(prompt.contains("ZERO"));
    }

    @Test
    void testAnalyzePortfolioServiceThrows() throws Exception {
        when(portfolioService.findPortfolioById(3)).thenThrow(new RuntimeException("db down"));

        String res = aiController.analyzePortfolio(3);

        assertTrue(res.startsWith("Error:"));
        assertTrue(res.contains("db down"));
    }

    @Test
    void testAnalyzePortfolioAiServiceThrows() throws Exception {
        when(portfolioService.findPortfolioById(6)).thenReturn(samplePortfolio);
        when(aiService.askAi(anyString())).thenThrow(new RuntimeException("ai failed"));

        String res = aiController.analyzePortfolio(6);

        assertTrue(res.startsWith("Error:"));
        assertTrue(res.contains("ai failed"));
    }
}
