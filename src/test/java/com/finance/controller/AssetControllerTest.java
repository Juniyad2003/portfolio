package com.finance.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.finance.entity.Asset;
import com.finance.exception.InvalidAssetIdException;
import com.finance.service.AssetService;

@ExtendWith(MockitoExtension.class)
public class AssetControllerTest {

    @Mock
    private AssetService assetService;

    @InjectMocks
    private AssetController assetController;

    private Asset sampleAsset;

    @BeforeEach
    void setUp() {
        sampleAsset = new Asset("ABC Corp", "STOCK", 100.0, 150.0, "MEDIUM", 10.0);
    }

    @Test
    void testGetAllAssets() {
        List<Asset> list = Collections.singletonList(sampleAsset);
        when(assetService.findAllAssets()).thenReturn(list);

        ResponseEntity<List<Asset>> resp = assetController.getAllAssets();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<Asset> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(sampleAsset, body.get(0));
        verify(assetService).findAllAssets();
    }

    @Test
    void testGetAssetByIdSuccess() throws InvalidAssetIdException {
        when(assetService.findAssetById(0)).thenReturn(sampleAsset);

        ResponseEntity<Asset> resp = assetController.getAssetById(0);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(sampleAsset, resp.getBody());
        verify(assetService).findAssetById(0);
    }

    @Test
    void testGetAssetByIdNotFound() throws InvalidAssetIdException {
        when(assetService.findAssetById(999)).thenThrow(new InvalidAssetIdException("not found"));

        assertThrows(InvalidAssetIdException.class, () -> assetController.getAssetById(999));
        verify(assetService).findAssetById(999);
    }

    @Test
    void testSaveAsset() {
        when(assetService.saveAsset(sampleAsset)).thenReturn(sampleAsset);

        ResponseEntity<Asset> resp = assetController.saveAsset(sampleAsset);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(sampleAsset, resp.getBody());
        verify(assetService).saveAsset(sampleAsset);
    }

    @Test
    void testUpdateAssetSuccess() throws InvalidAssetIdException {

        when(assetService.updateAsset(sampleAsset)).thenReturn(sampleAsset);

        ResponseEntity<Asset> resp = assetController.updateAsset(0, sampleAsset);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(sampleAsset, resp.getBody());
        verify(assetService).updateAsset(sampleAsset);
    }

    @Test
    void testUpdateAssetIdMismatch() {

        assertThrows(InvalidAssetIdException.class, () -> assetController.updateAsset(1, sampleAsset));
    }

    @Test
    void testDeleteAssetSuccess() throws InvalidAssetIdException {
        when(assetService.deleteAsset(0)).thenReturn(sampleAsset);

        ResponseEntity<Asset> resp = assetController.deleteAsset(0);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(sampleAsset, resp.getBody());
        verify(assetService).deleteAsset(0);
    }

    @Test
    void testDeleteAssetNotFound() throws InvalidAssetIdException {
        when(assetService.deleteAsset(5)).thenThrow(new InvalidAssetIdException("nope"));

        assertThrows(InvalidAssetIdException.class, () -> assetController.deleteAsset(5));
        verify(assetService).deleteAsset(5);
    }
}
