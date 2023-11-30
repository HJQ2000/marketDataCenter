package com.yfd.marketdatacenter.model;
import java.time.LocalDateTime;


public class MarketData {
    private String stockName;
    private String stockId;
    private double curPrice;
    private double absChange;
    private double perChange;
    private long dealCount;
    private double dealValue;
    private double maxPrice;
    private double minPrice;
    private double meanPrice;
    private long fetchTime;

    private LocalDateTime timeStampChina;

    public MarketData() {

    }

    public MarketData(String stockId, double curPrice, long fetchTime) {
        this.stockId = stockId;
        this.curPrice = curPrice;
        this.fetchTime = fetchTime;
    }

    public MarketData(String stockName, String stockId, double curPrice, double absChange, double perChange,
                      long dealCount, double dealValue, double maxPrice, double minPrice, double meanPrice, LocalDateTime timeStamp) {
        this.stockId = stockId;
        this.stockName = stockName;
        this.curPrice = curPrice;
        this.absChange = absChange;
        this.perChange = perChange;
        this.dealCount = dealCount;
        this.dealValue = dealValue;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.meanPrice = meanPrice;
        this.timeStampChina = timeStamp;
    }


    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public void setStockPrice(double curPrice) {
        this.curPrice = curPrice;
    }

    public void setStockDealCount(long dealCount) {
        this.dealCount = dealCount;
    }
    public void setStockDealValue(double dealValue) {
        this.dealValue = dealValue;
    }


    public double getStockPrice() {
        return this.curPrice;
    }

    public String getStockName() {
        return this.stockName;
    }

    public long getFetchTimeLocal() {
        return this.fetchTime;
    }

}
