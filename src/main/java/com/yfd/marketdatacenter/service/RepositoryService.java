package com.yfd.marketdatacenter.service;

import com.yfd.marketdatacenter.model.MarketDataMin;
import com.yfd.marketdatacenter.model.Stock;
import com.yfd.marketdatacenter.repository.MinDataRepository;
import com.yfd.marketdatacenter.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RepositoryService {
    private final MinDataRepository minDataRepository;
    private final StockRepository stockRepository;

    @Autowired
    public RepositoryService(MinDataRepository minDataRepository, StockRepository stockRepository){
        this.minDataRepository = minDataRepository;
        this.stockRepository = stockRepository;
    }

    public List<MarketDataMin> findByStockId(String stockId) {
        return minDataRepository.findByStockId(stockId);
    }

    public List<MarketDataMin> findByStockIdAndTimeStamp(String stockId, LocalDateTime timeStamp) {
        return minDataRepository.findByStockIdAndTimeStamp(stockId, timeStamp);
    }

    public Optional<Stock> findStock(String stockId) {
        return stockRepository.findById(stockId);
    }

    public void save(Stock stock) {
        stockRepository.save(stock);
    }

    public void save(MarketDataMin marketDataMin) {
        minDataRepository.save(marketDataMin);
    }
}
