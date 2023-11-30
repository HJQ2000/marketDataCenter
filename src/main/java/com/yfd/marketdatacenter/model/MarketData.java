package com.yfd.marketdatacenter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class MarketData {
    @Column(name = "stock_id")
    private String stockId;
    public MarketData(String stockId) {
        this.stockId = stockId;
    }

    public MarketData() {
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

}
