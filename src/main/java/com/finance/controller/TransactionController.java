package com.finance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finance.entity.Transaction;
import com.finance.exception.InvalidTransactionIdException;
import com.finance.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    TransactionService transactionService;
    com.finance.service.PortfolioService portfolioService;
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    public TransactionController(TransactionService transactionService,
                                 com.finance.service.PortfolioService portfolioService) {
        this.transactionService = transactionService;
        this.portfolioService = portfolioService;
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

    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(@RequestBody TradeRequest request) {
        log.info("BUY Request | portfolo={},symbol={},qty={}",request.getPortfolioId(),request.getSymbol(),request.getQuantity());
        try {
            String assetType = request.getAssetType() != null ? request.getAssetType() : "STOCK";
            System.out.println("Processing BUY request: " + request.getSymbol() + " (" + request.getQuantity()
                    + " shares) for portfolio ID: " + request.getPortfolioId() + ", Asset Type: " + assetType);
            portfolioService.buyStock(request.getPortfolioId(), request.getSymbol(), request.getQuantity(),
                    request.getPrice(), assetType);
            return new ResponseEntity<>("Buy transaction successful", HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Buy failed: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("Error buying stock: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellStock(@RequestBody TradeRequest request) {
        log.info("SELL Request | portfolo={},symbol={},qty={}",request.getPortfolioId(),request.getSymbol(),request.getQuantity());
        try {
            String assetType = request.getAssetType() != null ? request.getAssetType() : "STOCK";
            System.out.println("Processing SELL request: " + request.getSymbol() + " (" + request.getQuantity()
                    + " shares) for portfolio ID: " + request.getPortfolioId() + ", Asset Type: " + assetType);
            portfolioService.sellStock(request.getPortfolioId(), request.getSymbol(), request.getQuantity(),
                    request.getPrice(), assetType);
            return new ResponseEntity<>("Sell transaction successful", HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Sell failed: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("Error selling stock: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<org.springframework.data.domain.Page<Transaction>> getTransactionsByPortfolioId(
            @PathVariable int portfolioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("transactionDate").descending());
        return new ResponseEntity<>(transactionService.findTransactionsByPortfolioId(portfolioId, pageable),
                HttpStatus.OK);
    }

    static class TradeRequest {
        private int portfolioId;
        private String symbol;
        private double quantity;
        private double price;
        private String assetType;

        public int getPortfolioId() {
            return portfolioId;
        }

        public void setPortfolioId(int portfolioId) {
            this.portfolioId = portfolioId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getAssetType() {
            return assetType;
        }

        public void setAssetType(String assetType) {
            this.assetType = assetType;
        }
    }
}
