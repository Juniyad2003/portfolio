package com.finance.repo;

import com.finance.entity.Asset;
import com.finance.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepo extends JpaRepository<Asset, Integer> {
    public List<Asset> findAllByPortfolio(Portfolio portfolio);

    public org.springframework.data.domain.Page<Asset> findByPortfolio_Id(int portfolioId,
                                                                          org.springframework.data.domain.Pageable pageable);
}