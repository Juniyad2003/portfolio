package com.finance.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.finance.entity.Transaction;
import com.finance.exception.InvalidTransactionIdException;
import com.finance.repo.TransactionRepo;

@Service
public class TransactionService {

    private TransactionRepo transactionRepo;

    public TransactionService(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepo.save(transaction);
    }

    public Transaction findTransactionById(int id) throws InvalidTransactionIdException {
        Optional<Transaction> opt = transactionRepo.findById(id);
        if (opt.isEmpty()) {
            throw new InvalidTransactionIdException("Transaction ID " + id + " is not valid");
        }
        return opt.get();
    }

    public List<Transaction> findAllTransactions() {
        return transactionRepo.findAll();
    }

    public Transaction updateTransaction(Transaction transaction) throws InvalidTransactionIdException {
        findTransactionById(transaction.getId());
        return transactionRepo.save(transaction);
    }

    public Transaction deleteTransaction(int id) throws InvalidTransactionIdException {
        Transaction transaction = findTransactionById(id);
        transactionRepo.deleteById(id);
        return transaction;
    }

    public org.springframework.data.domain.Page<Transaction> findTransactionsByPortfolioId(int portfolioId,
                                                                                           org.springframework.data.domain.Pageable pageable) {
        return transactionRepo.findByAsset_Portfolio_Id(portfolioId, pageable);
    }
}