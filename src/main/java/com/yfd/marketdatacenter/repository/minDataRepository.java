package com.yfd.marketdatacenter.repository;

import com.yfd.marketdatacenter.model.MarketDataMin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface minDataRepository extends JpaRepository<MarketDataMin, Integer> {
    @Query(value="select * from min_stock where stock_id = ?1", nativeQuery = true)
    List<MarketDataMin> getMinDataGivenStock(String stockId);



}
