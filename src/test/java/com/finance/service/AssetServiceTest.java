package com.finance.service;

import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;
import com.finance.exception.InvalidAssetIdException;
import com.finance.repo.AssetRepo;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

    @Mock
    private AssetRepo assetRepo;

    @InjectMocks
    private AssetService assetService;

    private Asset sampleAsset;
    private Portfolio samplePortfolio;

    @BeforeEach
    void setUp() {
        sampleAsset = new Asset("ABC Corp", "STOCK", 100.0, 150.0, "MEDIUM", 10.0);
        samplePortfolio = new Portfolio();
        sampleAsset.setPortfolio(samplePortfolio);
    }

    @Test
    void testSaveAsset() {
        when(assetRepo.save(sampleAsset)).thenReturn(sampleAsset);

        Asset result = assetService.saveAsset(sampleAsset);

        assertNotNull(result);
        assertEquals("ABC Corp", result.getAssetName());
        verify(assetRepo, times(1)).save(sampleAsset);
    }

    @Test
    void testFindAssetsByPortfolio() {
        List<Asset> list = Collections.singletonList(sampleAsset);
        when(assetRepo.findAllByPortfolio(samplePortfolio)).thenReturn(list);

        List<Asset> result = assetService.findAssetsByPortfolio(samplePortfolio);

        assertEquals(1, result.size());
        assertEquals(sampleAsset, result.get(0));
        verify(assetRepo).findAllByPortfolio(samplePortfolio);
    }

    @Test
    void testFindAssetsByPortfolioId() {
        int portfolioId = 0;
        Page<Asset> page = new PageImpl<>(Collections.singletonList(sampleAsset));
        when(assetRepo.findByPortfolio_Id(eq(portfolioId), any(Pageable.class))).thenReturn(page);

        Page<Asset> result = assetService.findAssetsByPortfolioId(portfolioId, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(assetRepo).findByPortfolio_Id(eq(portfolioId), any(Pageable.class));
    }

    @Test
    void testFindAllAssetsByPortfolioId() {
        int portfolioId = 0;
        Page<Asset> page = new PageImpl<>(Collections.singletonList(sampleAsset));
        when(assetRepo.findByPortfolio_Id(eq(portfolioId), any(Pageable.class))).thenReturn(page);

        List<Asset> result = assetService.findAllAssetsByPortfolioId(portfolioId);

        assertEquals(1, result.size());
        assertEquals(sampleAsset, result.get(0));
        verify(assetRepo).findByPortfolio_Id(eq(portfolioId), any(Pageable.class));
    }

    @Test
    void testFindAllAssets() {
        List<Asset> all = Collections.singletonList(sampleAsset);
        when(assetRepo.findAll()).thenReturn(all);

        List<Asset> result = assetService.findAllAssets();

        assertEquals(1, result.size());
        verify(assetRepo).findAll();
    }

    @Test
    void testFindAssetByIdSuccess() throws InvalidAssetIdException {
        int id = 0;
        when(assetRepo.findById(id)).thenReturn(Optional.of(sampleAsset));

        Asset result = assetService.findAssetById(id);

        assertEquals(sampleAsset, result);
        verify(assetRepo).findById(id);
    }

    @Test
    void testFindAssetByIdNotFound() {
        int id = 999;
        when(assetRepo.findById(id)).thenReturn(Optional.empty());

        InvalidAssetIdException ex = assertThrows(InvalidAssetIdException.class, () -> assetService.findAssetById(id));
        assertTrue(ex.getMessage().contains("Asset ID"));
        verify(assetRepo).findById(id);
    }

    @Test
    void testUpdateAssetSuccess() throws InvalidAssetIdException {
        // asset.getId() returns 0 by default
        int id = 0;
        when(assetRepo.findById(id)).thenReturn(Optional.of(sampleAsset));
        when(assetRepo.save(sampleAsset)).thenReturn(sampleAsset);

        Asset result = assetService.updateAsset(sampleAsset);

        assertEquals(sampleAsset, result);
        verify(assetRepo).findById(id);
        verify(assetRepo).save(sampleAsset);
    }

    @Test
    void testUpdateAssetNotFound() {
        Asset assetWithDifferentId = new Asset("X", "ETF", 10, 11, "LOW", 1);

        when(assetRepo.findById(0)).thenReturn(Optional.empty());

        assertThrows(InvalidAssetIdException.class, () -> assetService.updateAsset(assetWithDifferentId));
        verify(assetRepo).findById(0);
    }

    @Test
    void testDeleteAssetSuccess() throws InvalidAssetIdException {
        int id = 0;
        when(assetRepo.findById(id)).thenReturn(Optional.of(sampleAsset));
        doNothing().when(assetRepo).deleteById(id);

        Asset deleted = assetService.deleteAsset(id);

        assertEquals(sampleAsset, deleted);
        verify(assetRepo).findById(id);
        verify(assetRepo).deleteById(id);
    }

    @Test
    void testDeleteAssetNotFound() {
        int id = 42;
        when(assetRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(InvalidAssetIdException.class, () -> assetService.deleteAsset(id));
        verify(assetRepo).findById(id);
    }
}
