package com.yfd.marketdatacenter.model;
import java.time.LocalDateTime;


public class MarketData {
    private String loc;
    private String stockName;
    private String stockId;
    private double curPrice;
    private double absChange;
    private double perChange;
    private long dealCount;
    private long dealAmount;
    private double maxPrice;
    private double minPrice;
    private double meanPrice;
    private long fetchTime;

    private LocalDateTime timeStampChina;

    public MarketData() {

    }

    public MarketData(String loc, String stockName, String stockId, double curPrice, long fetchTime) {
        this.loc = loc;
        this.stockId = stockId;
        this.stockName = stockName;
        this.curPrice = curPrice;
        this.fetchTime = fetchTime;
    }

    public MarketData(String loc, String stockName, String stockId, double curPrice, double absChange, double perChange,
                      long dealCount, long dealAmount, double maxPrice, double minPrice, double meanPrice, LocalDateTime timeStamp) {
        this.loc = loc;
        this.stockId = stockId;
        this.stockName = stockName;
        this.curPrice = curPrice;
        this.absChange = absChange;
        this.perChange = perChange;
        this.dealCount = dealCount;
        this.dealAmount = dealAmount;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.meanPrice = meanPrice;
        this.timeStampChina = timeStamp;
    }

    public void setLoc(String loc) {
        this.loc = loc;
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
