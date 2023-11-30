package com.yfd.marketdatacenter.controller;

import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.service.MarketDataFetcher;
import com.yfd.marketdatacenter.service.StockSymbols;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@EnableScheduling
public class MarketController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Save my Day!";
    }

   @Autowired
   private MarketDataFetcher marketDataFetcher;

   @GetMapping("/market-data-requested")
   public MarketData getMarketData(@RequestParam String stockSymbols) {
       long startTime = System.currentTimeMillis();
       MarketData md = marketDataFetcher.fetchAndProcessData(stockSymbols);
       System.out.println("Milliseconds difference: " + (System.currentTimeMillis() - startTime));
       System.out.println(md.getStockName() + ": " + md.getStockPrice());
       return md;
   }



    @GetMapping("/market-data-concurrent")
    public List<MarketData> getMarketDataConcurrent() {
        long startTime = System.currentTimeMillis();
        List<MarketData> result = marketDataFetcher.fetchAndProcessAll();
        System.out.println("Concurrent Time Cost(ms): " + (System.currentTimeMillis() - startTime)); //concurrent data fetcher for 20 stock takes about 2s，但是2000个还是要200s左右
        return result;
    }

}
