package com.finance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finance.entity.Asset;
import com.finance.exception.InvalidAssetIdException;
import com.finance.service.AssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/assets")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);

    AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("")
    public ResponseEntity<List<Asset>> getAllAssets() {
        log.info("GET /assets");
        List<Asset> assets = assetService.findAllAssets();
        return new ResponseEntity<>(assets, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable int id)
            throws InvalidAssetIdException {
        log.info("Get /assets/{}",id);
        Asset asset = assetService.findAssetById(id);
        return new ResponseEntity<>(asset, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Asset> saveAsset(@RequestBody Asset asset) {
        log.info("Get /assets | name={}",asset.getAssetName());
        Asset saved = assetService.saveAsset(asset);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(
            @PathVariable int id,
            @RequestBody Asset asset)
            throws InvalidAssetIdException {

        if (id != asset.getId()) {
            throw new InvalidAssetIdException(
                    "Asset id " + id + " is not matching with " + asset.getId());
        }

        Asset updated = assetService.updateAsset(asset);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Asset> deleteAsset(@PathVariable int id)
            throws InvalidAssetIdException {
        log.warn("DELETE /assets/{}",id);
        Asset asset = assetService.deleteAsset(id);
        return new ResponseEntity<>(asset, HttpStatus.OK);
    }
}