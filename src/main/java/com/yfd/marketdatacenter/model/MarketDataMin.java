package com.yfd.marketdatacenter.model;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name="min_stock")
public class MarketDataMin extends MarketData{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "cur_price")
    private double curPrice=0.0;
    @Column(name = "abs_change")
    private Double absChange=0.0;
    @Column(name = "per_change")
    private Double perChange=0.0;
    @Column(name = "deal_count")
    private Long dealCount = 0L;
    @Column(name = "deal_value")
    private Double dealValue = 0.0;
    @Column(name = "mean_price")
    private Double meanPrice = 0.0;
    @Column(name = "fetch_time")
    private Long fetchTime;
    @Column(name = "timestamp_china")
    private LocalDateTime timeStampChina;

    public MarketDataMin() {
        super();
    }

    public MarketDataMin(String stockId, double curPrice, long fetchTime, LocalDateTime time) {
        super(stockId);
        this.curPrice = curPrice;
        this.fetchTime = fetchTime;
        this.timeStampChina = time;
    }

    public MarketDataMin(String stockId, double curPrice, double absChange, double perChange,
                      long dealCount, double dealValue, double meanPrice, LocalDateTime timeStamp) {
        super(stockId);
        this.curPrice = curPrice;
        this.absChange = absChange;
        this.perChange = perChange;
        this.dealCount = dealCount;
        this.dealValue = dealValue;
        this.meanPrice = meanPrice;
        this.timeStampChina = timeStamp;
    }

    public String getStockId() {
        return super.getStockId();
    }

    // 子类方法中修改父类属性
    public void setStockId(String id) {
        super.setStockId(id);
    }
    public double getCurPrice() {
        return curPrice;
    }

    public void setCurPrice(double curPrice) {
        this.curPrice = curPrice;
    }

    public double getAbsChange() {
        return absChange;
    }

    public void setAbsChange(double absChange) {
        this.absChange = absChange;
    }

    public double getPerChange() {
        return perChange;
    }

    public void setPerChange(double perChange) {
        this.perChange = perChange;
    }

    public long getDealCount() {
        return dealCount;
    }

    public void setDealCount(long dealCount) {
        this.dealCount = dealCount;
    }

    public double getDealValue() {
        return dealValue;
    }

    public void setDealValue(double dealValue) {
        this.dealValue = dealValue;
    }

    public double getMeanPrice() {
        return meanPrice;
    }

    public void setMeanPrice(double meanPrice) {
        this.meanPrice = meanPrice;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public void setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
    }

    public LocalDateTime getTimeStampChina() {
        return timeStampChina;
    }

    public void setTimeStampChina(LocalDateTime timeStampChina) {
        this.timeStampChina = timeStampChina;
    }
}
