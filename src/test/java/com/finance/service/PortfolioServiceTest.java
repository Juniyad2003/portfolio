package com.finance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;
import com.finance.exception.InvalidPortfolioIdException;
import com.finance.repo.AssetRepo;
import com.finance.repo.PortfolioRepo;
import com.finance.repo.TransactionRepo;

@ExtendWith(MockitoExtension.class)
public class PortfolioServiceTest {

    @Mock
    private PortfolioRepo portfolioRepo;

    @Mock
    private AssetRepo assetRepo;

    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private PortfolioService portfolioService;

    private Portfolio samplePortfolio;
    private Asset sampleAsset;

    @BeforeEach
    void setUp() {
        samplePortfolio = new Portfolio();
        samplePortfolio.setPortfolioName("MyPortfolio");
        samplePortfolio.setTotalInvestment(1000.0);
        samplePortfolio.setCurrentValue(1000.0);
        samplePortfolio.setTotalProfitLoss(0.0);

        sampleAsset = new Asset("ABC", "STOCK", 100.0, 150.0, "MEDIUM", 10.0);
        sampleAsset.setPortfolio(samplePortfolio);
        samplePortfolio.setAssets(Collections.singletonList(sampleAsset));
    }

    @Test
    void testSavePortfolio() {
        when(portfolioRepo.save(samplePortfolio)).thenReturn(samplePortfolio);

        Portfolio saved = portfolioService.savePortfolio(samplePortfolio);

        assertEquals(samplePortfolio, saved);
        verify(portfolioRepo).save(samplePortfolio);
    }

    @Test
    void testFindPortfolioByIdSuccess() throws InvalidPortfolioIdException {
        when(portfolioRepo.findById(1)).thenReturn(Optional.of(samplePortfolio));

        Portfolio p = portfolioService.findPortfolioById(1);

        assertEquals(samplePortfolio, p);
        verify(portfolioRepo).findById(1);
    }

    @Test
    void testFindPortfolioByIdNotFound() {
        when(portfolioRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(InvalidPortfolioIdException.class, () -> portfolioService.findPortfolioById(99));
        verify(portfolioRepo).findById(99);
    }

    @Test
    void testFindAllPortfolios() {
        List<Portfolio> all = Collections.singletonList(samplePortfolio);
        when(portfolioRepo.findAll()).thenReturn(all);

        List<Portfolio> result = portfolioService.findAllPortfolios();

        assertEquals(1, result.size());
        verify(portfolioRepo).findAll();
    }

    @Test
    void testUpdatePortfolioSuccess() throws InvalidPortfolioIdException {
        Portfolio toUpdate = samplePortfolio;
        when(portfolioRepo.findById(0)).thenReturn(Optional.of(samplePortfolio));
        when(portfolioRepo.save(toUpdate)).thenReturn(toUpdate);

        Portfolio result = portfolioService.updatePortfolio(toUpdate);

        assertEquals(toUpdate, result);
        verify(portfolioRepo).findById(0);
        verify(portfolioRepo).save(toUpdate);
    }

    @Test
    void testUpdatePortfolioNotFound() {
        Portfolio toUpdate = new Portfolio();
        when(portfolioRepo.findById(0)).thenReturn(Optional.empty());

        assertThrows(InvalidPortfolioIdException.class, () -> portfolioService.updatePortfolio(toUpdate));
        verify(portfolioRepo).findById(0);
    }

    @Test
    void testDeletePortfolioSuccess() throws InvalidPortfolioIdException {
        when(portfolioRepo.findById(5)).thenReturn(Optional.of(samplePortfolio));
        doNothing().when(portfolioRepo).deleteById(5);

        Portfolio deleted = portfolioService.deletePortfolio(5);

        assertEquals(samplePortfolio, deleted);
        verify(portfolioRepo).findById(5);
        verify(portfolioRepo).deleteById(5);
    }

    @Test
    void testDeletePortfolioNotFound() {
        when(portfolioRepo.findById(6)).thenReturn(Optional.empty());

        assertThrows(InvalidPortfolioIdException.class, () -> portfolioService.deletePortfolio(6));
        verify(portfolioRepo).findById(6);
    }

    @Test
    void testBuyStockCreatesNewAssetWhenNotExist() throws InvalidPortfolioIdException {
        Portfolio p = new Portfolio();
        p.setAssets(null);
        p.setTotalInvestment(0);
        p.setCurrentValue(0);
        when(portfolioRepo.findById(10)).thenReturn(Optional.of(p));

        when(assetRepo.save(any(Asset.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioRepo.save(any(Portfolio.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.buyStock(10, "NEW", 2.0, 50.0, "STOCK");

        verify(portfolioRepo).findById(10);
        verify(assetRepo).save(any(Asset.class));
        verify(transactionRepo).save(any());
        verify(portfolioRepo).save(any(Portfolio.class));
    }

    @Test
    void testBuyStockUpdatesExistingAsset() throws InvalidPortfolioIdException {
        when(portfolioRepo.findById(11)).thenReturn(Optional.of(samplePortfolio));
        when(assetRepo.save(any(Asset.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioRepo.save(any(Portfolio.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.buyStock(11, "ABC", 5.0, 20.0, null);

        // After buying 5 more of ABC (existing quantity 10), new quantity should be 15
        assertEquals(15.0, samplePortfolio.getAssets().get(0).getQuantity());
        verify(assetRepo).save(any(Asset.class));
        verify(transactionRepo).save(any());
        verify(portfolioRepo).save(any(Portfolio.class));
    }

    @Test
    void testSellStockSuccess() throws InvalidPortfolioIdException {
        when(portfolioRepo.findById(20)).thenReturn(Optional.of(samplePortfolio));
        when(assetRepo.save(any(Asset.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(portfolioRepo.save(any(Portfolio.class))).thenAnswer(inv -> inv.getArgument(0));

        portfolioService.sellStock(20, "ABC", 5.0, 10.0, null);

        assertEquals(5.0, samplePortfolio.getAssets().get(0).getQuantity());
        verify(assetRepo).save(any(Asset.class));
        verify(transactionRepo).save(any());
        verify(portfolioRepo).save(any(Portfolio.class));
    }

    @Test
    void testSellStockInsufficientThrows() {
        when(portfolioRepo.findById(21)).thenReturn(Optional.of(samplePortfolio));

        assertThrows(RuntimeException.class, () -> portfolioService.sellStock(21, "ABC", 20.0, 10.0, null));

        verify(portfolioRepo).findById(21);
    }
}
