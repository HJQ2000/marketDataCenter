package com.yfd.marketdatacenter.controller;

import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.model.MarketDataMin;
import com.yfd.marketdatacenter.service.HttpQTMarketDataFetcher;
import com.yfd.marketdatacenter.service.HttpQTMinBeforeDataFetcher;
import com.yfd.marketdatacenter.service.MarketDataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@EnableScheduling
public class MarketController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Save my Day!";
    }


   private MarketDataFetcher marketDataFetcher = new HttpQTMarketDataFetcher();

   @GetMapping("/market-data-requested")
   public MarketData getMarketData(@RequestParam String stockSymbols) {
       long startTime = System.currentTimeMillis();
       MarketDataMin md = (MarketDataMin)marketDataFetcher.fetchAndProcessData(stockSymbols);
       System.out.println("Milliseconds difference: " + (System.currentTimeMillis() - startTime));
       System.out.println(md.getStockId() + ": " + md.getCurPrice());
       return md;
   }

    @GetMapping("/market-data-concurrent")
    public List<MarketData> getMarketDataConcurrent() {
        long startTime = System.currentTimeMillis();
        List<MarketData> result = marketDataFetcher.fetchAndProcessAll();
        System.out.println("Concurrent Time Cost(ms): " + (System.currentTimeMillis() - startTime)); //concurrent data fetcher for 20 stock takes about 2s，但是2000个还是要200s左右
        return result;
    }

    private MarketDataFetcher minBeforeDatFetcher = new HttpQTMinBeforeDataFetcher();
    @GetMapping("/min-before")
    public List<MarketData> getMinBeforeData(@RequestParam String stockSymbols) {
        long startTime = System.currentTimeMillis();
        List<MarketData> mdList = minBeforeDatFetcher.fetchAndProcessOne(stockSymbols);
        System.out.println("Milliseconds difference: " + (System.currentTimeMillis() - startTime));
        return mdList;
    }

}
