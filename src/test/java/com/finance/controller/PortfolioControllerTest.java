package com.finance.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;
import com.finance.exception.InvalidPortfolioIdException;
import com.finance.service.AssetService;
import com.finance.service.PortfolioService;
import com.finance.dto.PerformancePoint;

@ExtendWith(MockitoExtension.class)
public class PortfolioControllerTest {

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private PortfolioController portfolioController;

    private Portfolio samplePortfolio;
    private Asset sampleAsset;

    @BeforeEach
    void setUp() {
        samplePortfolio = new Portfolio(1, "Alice", "a@x.com", "LONG_TERM", "MEDIUM", "AlicePortfolio");
        samplePortfolio.setCurrentValue(1000.0);
        samplePortfolio.setTotalInvestment(900.0);

        sampleAsset = new Asset("ABC", "STOCK", 100.0, 120.0, "MEDIUM", 10.0);
        sampleAsset.setQuantity(5.0);
        sampleAsset.setPortfolio(samplePortfolio);
    }

    @Test
    void testGetAllPortfolios() throws InvalidPortfolioIdException {
        when(portfolioService.findAllPortfolios()).thenReturn(Collections.singletonList(samplePortfolio));
        when(assetService.findAllAssetsByPortfolioId(eq(1))).thenReturn(Collections.emptyList());
        doReturn(samplePortfolio).when(portfolioService).updatePortfolio(any(Portfolio.class));

        ResponseEntity<List<Portfolio>> resp = portfolioController.getAllPortfolios();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<Portfolio> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        verify(portfolioService).findAllPortfolios();
        verify(assetService).findAllAssetsByPortfolioId(1);
        try {
            verify(portfolioService).updatePortfolio(any(Portfolio.class));
        } catch (InvalidPortfolioIdException e) {
            fail("updatePortfolio verification threw: " + e.getMessage());
        }
    }

    @Test
    void testGetPortfolioByIdSuccess() throws InvalidPortfolioIdException {
        when(portfolioService.findPortfolioById(1)).thenReturn(samplePortfolio);

        ResponseEntity<Portfolio> resp = portfolioController.getPortfolioById(1);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(samplePortfolio, resp.getBody());
        verify(portfolioService).findPortfolioById(1);
    }

    @Test
    void testGetPortfolioByIdNotFound() throws InvalidPortfolioIdException {
        when(portfolioService.findPortfolioById(99)).thenThrow(new InvalidPortfolioIdException("not found"));

        assertThrows(InvalidPortfolioIdException.class, () -> portfolioController.getPortfolioById(99));
        verify(portfolioService).findPortfolioById(99);
    }

    @Test
    void testGetAssetsByPortfolio() throws InvalidPortfolioIdException {
        when(portfolioService.findPortfolioById(1)).thenReturn(samplePortfolio);
        when(assetService.findAssetsByPortfolioId(eq(1), any())).thenReturn(new PageImpl<>(Collections.singletonList(sampleAsset)));

        ResponseEntity<org.springframework.data.domain.Page<Asset>> resp = portfolioController.getAssetsByPortfolio(1, 0, 10);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().getTotalElements());
        verify(portfolioService).findPortfolioById(1);
        verify(assetService).findAssetsByPortfolioId(eq(1), any());
    }

    @Test
    void testSavePortfolio() {
        when(portfolioService.savePortfolio(samplePortfolio)).thenReturn(samplePortfolio);

        ResponseEntity<Portfolio> resp = portfolioController.savePortfolio(samplePortfolio);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(samplePortfolio, resp.getBody());
        verify(portfolioService).savePortfolio(samplePortfolio);
    }

    @Test
    void testUpdatePortfolioSuccess() throws InvalidPortfolioIdException {
        // samplePortfolio.getId() == 1
        doReturn(samplePortfolio).when(portfolioService).updatePortfolio(samplePortfolio);

        ResponseEntity<Portfolio> resp = portfolioController.updatePortfolio(1, samplePortfolio);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(samplePortfolio, resp.getBody());
        try {
            verify(portfolioService).updatePortfolio(samplePortfolio);
        } catch (InvalidPortfolioIdException e) {
            fail("updatePortfolio verification threw: " + e.getMessage());
        }
    }

    @Test
    void testUpdatePortfolioIdMismatch() {
        assertThrows(InvalidPortfolioIdException.class, () -> portfolioController.updatePortfolio(2, samplePortfolio));
    }

    @Test
    void testDeletePortfolioSuccess() throws InvalidPortfolioIdException {
        when(portfolioService.deletePortfolio(1)).thenReturn(samplePortfolio);

        ResponseEntity<Portfolio> resp = portfolioController.deletePortfolio(1);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(samplePortfolio, resp.getBody());
        verify(portfolioService).deletePortfolio(1);
    }

    @Test
    void testDeletePortfolioNotFound() throws InvalidPortfolioIdException {
        when(portfolioService.deletePortfolio(5)).thenThrow(new InvalidPortfolioIdException("nope"));

        assertThrows(InvalidPortfolioIdException.class, () -> portfolioController.deletePortfolio(5));
        verify(portfolioService).deletePortfolio(5);
    }

    @Test
    void testGetPortfolioPerformanceDefaultRange() throws InvalidPortfolioIdException {
        samplePortfolio.setRiskPreference("MEDIUM");
        samplePortfolio.setCurrentValue(2000.0);
        when(portfolioService.findPortfolioById(1)).thenReturn(samplePortfolio);

        ResponseEntity<List<PerformancePoint>> resp = portfolioController.getPortfolioPerformance(1, "1M");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<PerformancePoint> points = resp.getBody();
        assertNotNull(points);

        assertEquals(30, points.size());

        assertTrue(points.get(0).getDate().isBefore(points.get(points.size() - 1).getDate()) || points.get(0).getDate().isEqual(points.get(points.size() - 1).getDate()));
        verify(portfolioService).findPortfolioById(1);
    }
}
