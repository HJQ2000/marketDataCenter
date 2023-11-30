package com.yfd.marketdatacenter.service;

import com.yfd.marketdatacenter.model.MarketData;

import java.util.List;

public interface MarketDataFetcher {
    MarketData fetchAndProcessData(String stockSymbol);
    List<MarketData> fetchAndProcessAll();
}
