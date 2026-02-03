package com.finance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finance.entity.Transaction;
import com.finance.exception.InvalidTransactionIdException;
import com.finance.service.TransactionService;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.findAllTransactions();
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/test")
    public String test() {
        return "Transaction controller working";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable int id)
            throws InvalidTransactionIdException {

        Transaction transaction = transactionService.findTransactionById(id);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Transaction> saveTransaction(@RequestBody Transaction transaction) {
        Transaction saved = transactionService.saveTransaction(transaction);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable int id,
            @RequestBody Transaction transaction)
            throws InvalidTransactionIdException {

        if (id != transaction.getId()) {
            throw new InvalidTransactionIdException(
                    "Transaction id " + id + " is not matching with " + transaction.getId());
        }

        Transaction updated = transactionService.updateTransaction(transaction);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Transaction> deleteTransaction(@PathVariable int id)
            throws InvalidTransactionIdException {

        Transaction transaction = transactionService.deleteTransaction(id);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }
}
