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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.finance.entity.Transaction;
import com.finance.entity.Asset;
import com.finance.exception.InvalidTransactionIdException;
import com.finance.repo.TransactionRepo;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepo transactionRepo;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new Transaction();
        sampleTransaction.setTransactionType("BUY");
        sampleTransaction.setQuantity(5.0);
        sampleTransaction.setAmount(500.0);

        Asset sampleAsset = new Asset("XYZ", "STOCK", 200.0, 250.0, "LOW", 20.0);
        sampleTransaction.setAsset(sampleAsset);
    }

    @Test
    void testSaveTransaction() {
        when(transactionRepo.save(sampleTransaction)).thenReturn(sampleTransaction);

        Transaction saved = transactionService.saveTransaction(sampleTransaction);

        assertEquals(sampleTransaction, saved);
        verify(transactionRepo).save(sampleTransaction);
    }

    @Test
    void testFindTransactionByIdSuccess() throws InvalidTransactionIdException {
        when(transactionRepo.findById(1)).thenReturn(Optional.of(sampleTransaction));

        Transaction t = transactionService.findTransactionById(1);

        assertEquals(sampleTransaction, t);
        verify(transactionRepo).findById(1);
    }

    @Test
    void testFindTransactionByIdNotFound() {
        when(transactionRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(InvalidTransactionIdException.class, () -> transactionService.findTransactionById(99));
        verify(transactionRepo).findById(99);
    }

    @Test
    void testFindAllTransactions() {
        List<Transaction> all = Collections.singletonList(sampleTransaction);
        when(transactionRepo.findAll()).thenReturn(all);

        List<Transaction> result = transactionService.findAllTransactions();

        assertEquals(1, result.size());
        verify(transactionRepo).findAll();
    }

    @Test
    void testUpdateTransactionSuccess() throws InvalidTransactionIdException {

        when(transactionRepo.findById(0)).thenReturn(Optional.of(sampleTransaction));
        when(transactionRepo.save(sampleTransaction)).thenReturn(sampleTransaction);

        Transaction updated = transactionService.updateTransaction(sampleTransaction);

        assertEquals(sampleTransaction, updated);
        verify(transactionRepo).findById(0);
        verify(transactionRepo).save(sampleTransaction);
    }

    @Test
    void testUpdateTransactionNotFound() {
        Transaction newTx = new Transaction();
        when(transactionRepo.findById(0)).thenReturn(Optional.empty());

        assertThrows(InvalidTransactionIdException.class, () -> transactionService.updateTransaction(newTx));
        verify(transactionRepo).findById(0);
    }

    @Test
    void testDeleteTransactionSuccess() throws InvalidTransactionIdException {
        when(transactionRepo.findById(7)).thenReturn(Optional.of(sampleTransaction));
        doNothing().when(transactionRepo).deleteById(7);

        Transaction deleted = transactionService.deleteTransaction(7);

        assertEquals(sampleTransaction, deleted);
        verify(transactionRepo).findById(7);
        verify(transactionRepo).deleteById(7);
    }

    @Test
    void testDeleteTransactionNotFound() {
        when(transactionRepo.findById(8)).thenReturn(Optional.empty());

        assertThrows(InvalidTransactionIdException.class, () -> transactionService.deleteTransaction(8));
        verify(transactionRepo).findById(8);
    }

    @Test
    void testFindTransactionsByPortfolioIdPaged() {
        int portfolioId = 3;
        Page<Transaction> page = new PageImpl<>(Collections.singletonList(sampleTransaction));
        when(transactionRepo.findByAsset_Portfolio_Id(eq(portfolioId), any(Pageable.class))).thenReturn(page);

        Page<Transaction> result = transactionService.findTransactionsByPortfolioId(portfolioId, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(transactionRepo).findByAsset_Portfolio_Id(eq(portfolioId), any(Pageable.class));
    }
}
