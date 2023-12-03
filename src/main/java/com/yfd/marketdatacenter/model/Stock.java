package com.yfd.marketdatacenter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name="stock_name")
public class Stock {
    @Id
    private String StockId;

    private String StockName;

    public Stock(String stockId, String stockName) {
        StockId = stockId;
        StockName = stockName;
    }

    public Stock() {

    }

    public String getStockId() {
        return StockId;
    }

    public void setStockId(String stockId) {
        StockId = stockId;
    }

    public String getStockName() {
        return StockName;
    }

    public void setStockName(String stockName) {
        StockName = stockName;
    }
}
