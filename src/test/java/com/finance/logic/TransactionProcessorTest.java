package com.finance.logic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.finance.entity.Asset;
import com.finance.entity.Transaction;

public class TransactionProcessorTest {

    @Test
    void testApplyBuyTransaction() {
        Asset a = new Asset("A", "STOCK", 100.0, 100.0, "MEDIUM", 10.0);
        Transaction tx = new Transaction();
        tx.setTransactionType("BUY");
        tx.setAmount(50.0);

        TransactionProcessor.applyTransaction(tx, a);

        assertEquals(150.0, a.getInvestedAmount(), 0.0001);
        assertEquals(150.0, a.getCurrentAmount(), 0.0001);
    }

    @Test
    void testApplySellTransaction() {
        Asset a = new Asset("A", "STOCK", 100.0, 200.0, "MEDIUM", 10.0);
        Transaction tx = new Transaction();
        tx.setTransactionType("SELL");
        tx.setAmount(30.0);

        TransactionProcessor.applyTransaction(tx, a);

        assertEquals(200.0 - 30.0, a.getCurrentAmount(), 0.0001);
    }
}
