package com.finance.repo;

import com.finance.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByAsset_Portfolio_Id(int portfolioId, Pageable pageable);
}