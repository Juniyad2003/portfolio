package com.finance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;
import com.finance.exception.InvalidAssetIdException;
import com.finance.repo.AssetRepo;

@Service
public class AssetService {

    private AssetRepo assetRepo;

    public AssetService(AssetRepo assetRepo) {
        this.assetRepo = assetRepo;
    }

    public Asset saveAsset(Asset asset) {
        Asset savedAsset = assetRepo.save(asset);
        System.out.println("Saved asset : " + savedAsset);
        return savedAsset;
    }

    public List<Asset> findAssetsByPortfolio(Portfolio portfolio) {
        return assetRepo.findAllByPortfolio(portfolio);
    }

    public org.springframework.data.domain.Page<Asset> findAssetsByPortfolioId(int portfolioId,
                                                                               org.springframework.data.domain.Pageable pageable) {
        return assetRepo.findByPortfolio_Id(portfolioId, pageable);
    }

    public List<Asset> findAllAssetsByPortfolioId(int portfolioId) {
        return assetRepo.findByPortfolio_Id(portfolioId, org.springframework.data.domain.Pageable.unpaged())
                .getContent();
    }

    public List<Asset> findAllAssets() {
        return assetRepo.findAll();
    }

    public Asset findAssetById(int id) throws InvalidAssetIdException {
        Optional<Asset> opt = assetRepo.findById(id);
        if (opt.isEmpty()) {
            throw new InvalidAssetIdException("Asset ID " + id + " is not valid");
        }
        return opt.get();
    }

    public Asset updateAsset(Asset asset) throws InvalidAssetIdException {
        findAssetById(asset.getId());
        return assetRepo.save(asset);
    }

    public Asset deleteAsset(int id) throws InvalidAssetIdException {
        Asset asset = findAssetById(id);
        assetRepo.deleteById(id);
        return asset;
    }
}