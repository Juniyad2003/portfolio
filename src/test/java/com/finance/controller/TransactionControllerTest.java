package com.finance.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import com.finance.exception.InvalidPortfolioIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.finance.entity.Transaction;
import com.finance.entity.Asset;
import com.finance.exception.InvalidTransactionIdException;
import com.finance.service.TransactionService;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private com.finance.service.PortfolioService portfolioService;

    @InjectMocks
    private TransactionController transactionController;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new Transaction();
        sampleTransaction.setTransactionType("BUY");
        sampleTransaction.setQuantity(2.0);
        sampleTransaction.setAmount(200.0);
        sampleTransaction.setId(0); // default id
        Asset asset = new Asset("ABC", "STOCK", 100.0, 120.0, "MEDIUM", 10.0);
        sampleTransaction.setAsset(asset);
    }

    @Test
    void testHealthTestEndpoint() {
        String res = transactionController.test();
        assertEquals("Transaction controller working", res);
    }

    @Test
    void testGetAllTransactions() {
        when(transactionService.findAllTransactions()).thenReturn(Collections.singletonList(sampleTransaction));

        ResponseEntity<List<Transaction>> resp = transactionController.getAllTransactions();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().size());
        verify(transactionService).findAllTransactions();
    }

    @Test
    void testGetTransactionByIdSuccess() throws InvalidTransactionIdException {
        when(transactionService.findTransactionById(1)).thenReturn(sampleTransaction);

        ResponseEntity<Transaction> resp = transactionController.getTransactionById(1);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(sampleTransaction, resp.getBody());
        verify(transactionService).findTransactionById(1);
    }

    @Test
    void testGetTransactionByIdNotFound() throws InvalidTransactionIdException {
        when(transactionService.findTransactionById(99)).thenThrow(new InvalidTransactionIdException("not found"));

        assertThrows(InvalidTransactionIdException.class, () -> transactionController.getTransactionById(99));
        verify(transactionService).findTransactionById(99);
    }

    @Test
    void testSaveTransaction() {
        when(transactionService.saveTransaction(sampleTransaction)).thenReturn(sampleTransaction);

        ResponseEntity<Transaction> resp = transactionController.saveTransaction(sampleTransaction);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(sampleTransaction, resp.getBody());
        verify(transactionService).saveTransaction(sampleTransaction);
    }

    @Test
    void testUpdateTransactionSuccess() throws InvalidTransactionIdException {
        sampleTransaction.setId(5);
        doReturn(sampleTransaction).when(transactionService).updateTransaction(sampleTransaction);

        ResponseEntity<Transaction> resp = transactionController.updateTransaction(5, sampleTransaction);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(sampleTransaction, resp.getBody());
        try {
            verify(transactionService).updateTransaction(sampleTransaction);
        } catch (InvalidTransactionIdException e) {
            fail("update verification threw: " + e.getMessage());
        }
    }

    @Test
    void testUpdateTransactionIdMismatch() {
        sampleTransaction.setId(2);
        assertThrows(InvalidTransactionIdException.class, () -> transactionController.updateTransaction(3, sampleTransaction));
    }

    @Test
    void testDeleteTransactionSuccess() throws InvalidTransactionIdException {
        when(transactionService.deleteTransaction(4)).thenReturn(sampleTransaction);

        ResponseEntity<Transaction> resp = transactionController.deleteTransaction(4);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(sampleTransaction, resp.getBody());
        verify(transactionService).deleteTransaction(4);
    }

    @Test
    void testDeleteTransactionNotFound() throws InvalidTransactionIdException {
        when(transactionService.deleteTransaction(7)).thenThrow(new InvalidTransactionIdException("nope"));

        assertThrows(InvalidTransactionIdException.class, () -> transactionController.deleteTransaction(7));
        verify(transactionService).deleteTransaction(7);
    }

    @Test
    void testBuyStockSuccess() {
        TransactionController.TradeRequest req = new TransactionController.TradeRequest();
        req.setPortfolioId(1);
        req.setSymbol("ABC");
        req.setQuantity(1.0);
        req.setPrice(100.0);
        req.setAssetType(null);

        try {
            doNothing().when(portfolioService).buyStock(eq(1), eq("ABC"), eq(1.0), eq(100.0), anyString());
        } catch (InvalidPortfolioIdException e) {
            fail("stubbing buyStock threw: " + e.getMessage());
        }

        ResponseEntity<?> resp = transactionController.buyStock(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Buy transaction successful", resp.getBody());
        try {
            verify(portfolioService).buyStock(eq(1), eq("ABC"), eq(1.0), eq(100.0), anyString());
        } catch (InvalidPortfolioIdException e) {
            fail("verify buyStock threw: " + e.getMessage());
        }
    }

    @Test
    void testBuyStockFailure() {
        TransactionController.TradeRequest req = new TransactionController.TradeRequest();
        req.setPortfolioId(2);
        req.setSymbol("FAIL");
        req.setQuantity(1.0);
        req.setPrice(50.0);

        try {
            doThrow(new RuntimeException("buy failed")).when(portfolioService).buyStock(eq(2), anyString(), anyDouble(), anyDouble(), anyString());
        } catch (InvalidPortfolioIdException e) {
            throw new RuntimeException(e);
        }

        ResponseEntity<?> resp = transactionController.buyStock(req);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Object body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.toString().contains("Error buying stock"));
        try {
            verify(portfolioService).buyStock(eq(2), anyString(), anyDouble(), anyDouble(), anyString());
        } catch (InvalidPortfolioIdException e) {
            fail("verify buyStock threw: " + e.getMessage());
        }
    }

    @Test
    void testSellStockSuccess() {
        TransactionController.TradeRequest req = new TransactionController.TradeRequest();
        req.setPortfolioId(3);
        req.setSymbol("ABC");
        req.setQuantity(1.0);
        req.setPrice(100.0);

        try {
            doNothing().when(portfolioService).sellStock(eq(3), eq("ABC"), eq(1.0), eq(100.0), anyString());
        } catch (InvalidPortfolioIdException e) {
            fail("stubbing sellStock threw: " + e.getMessage());
        }

        ResponseEntity<?> resp = transactionController.sellStock(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Sell transaction successful", resp.getBody());
        try {
            verify(portfolioService).sellStock(eq(3), eq("ABC"), eq(1.0), eq(100.0), anyString());
        } catch (InvalidPortfolioIdException e) {
            fail("verify sellStock threw: " + e.getMessage());
        }
    }

    @Test
    void testSellStockFailure() {
        TransactionController.TradeRequest req = new TransactionController.TradeRequest();
        req.setPortfolioId(4);
        req.setSymbol("ABC");
        req.setQuantity(1000.0);
        req.setPrice(100.0);

        try {
            doThrow(new RuntimeException("insufficient")).when(portfolioService).sellStock(eq(4), anyString(), anyDouble(), anyDouble(), anyString());
        } catch (InvalidPortfolioIdException e) {
            fail("stubbing sellStock threw: " + e.getMessage());
        }

        ResponseEntity<?> resp = transactionController.sellStock(req);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        Object body = resp.getBody();
        assertNotNull(body);
        assertTrue(body.toString().contains("Error selling stock"));
        try {
            verify(portfolioService).sellStock(eq(4), anyString(), anyDouble(), anyDouble(), anyString());
        } catch (InvalidPortfolioIdException e) {
            fail("verify sellStock threw: " + e.getMessage());
        }
    }

    @Test
    void testGetTransactionsByPortfolioId() {
        when(transactionService.findTransactionsByPortfolioId(eq(5), any())).thenReturn(new PageImpl<>(Collections.singletonList(sampleTransaction)));

        ResponseEntity<org.springframework.data.domain.Page<Transaction>> resp = transactionController.getTransactionsByPortfolioId(5, 0, 10);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().getTotalElements());
        verify(transactionService).findTransactionsByPortfolioId(eq(5), any());
    }
}
