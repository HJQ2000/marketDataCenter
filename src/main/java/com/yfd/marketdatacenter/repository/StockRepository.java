package com.yfd.marketdatacenter.repository;

import com.yfd.marketdatacenter.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, String> {

}

