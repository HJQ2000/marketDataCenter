package com.yfd.marketdatacenter.repository;

import com.yfd.marketdatacenter.model.MarketDataMin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MinDataRepository extends JpaRepository<MarketDataMin, Integer> {
    @Query(value="select * from min_stock where stock_id = ?1", nativeQuery = true)
    List<MarketDataMin> findByStockId(String stockId);
    @Query(value="select * from min_stock where stock_id = ?1 and timestamp_china = ?2", nativeQuery = true)
    List<MarketDataMin> findByStockIdAndTimeStamp(String stockId, LocalDateTime timeStamp);

}

