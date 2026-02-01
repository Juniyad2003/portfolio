package com.finance.logic;

import com.finance.entity.Asset;
import com.finance.entity.Transaction;

public class TransactionProcessor {

    public static void applyTransaction(Transaction tx, Asset asset) {

        if (tx.getTransactionType().equalsIgnoreCase("BUY")) {

            asset.setInvestedAmount(
                    asset.getInvestedAmount() + tx.getAmount()
            );

            asset.setCurrentAmount(
                    asset.getCurrentAmount() + tx.getAmount()
            );

        } else if (tx.getTransactionType().equalsIgnoreCase("SELL")) {

            asset.setCurrentAmount(
                    asset.getCurrentAmount() - tx.getAmount()
            );
        }
    }
}
