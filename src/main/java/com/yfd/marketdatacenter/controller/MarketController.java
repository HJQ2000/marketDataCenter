package com.yfd.marketdatacenter.controller;

import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.model.MarketDataMin;
import com.yfd.marketdatacenter.repository.MinDataRepository;
import com.yfd.marketdatacenter.service.HttpQTMarketDataFetcher;
import com.yfd.marketdatacenter.service.HttpQTMinBeforeDataFetcher;
import com.yfd.marketdatacenter.service.MarketDataFetcher;
import com.yfd.marketdatacenter.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
   private HttpQTMarketDataFetcher marketDataFetcher;

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
        System.out.println("Concurrent Time Cost(ms) for fetch: " + (System.currentTimeMillis() - startTime)); //concurrent data fetcher for 20 stock takes about 2s，但是2000个还是要200s左右
//        startTime = System.currentTimeMillis();
//        List<MarketDataMin> resultMin = result.stream()
//                .map(MarketDataMin.class::cast)
//                .collect(Collectors.toList());
//        CompletableFuture.runAsync(() -> {
//            repositoryService.saveAll(resultMin);
//            System.out.println("Saving all to MySQL: " + resultMin);
//        });
//        System.out.println("Concurrent Time Cost(ms) for save: " + (System.currentTimeMillis() - startTime));
        return result;
    }
    @Autowired
    private HttpQTMinBeforeDataFetcher minBeforeDataFetcher;
    @GetMapping("/min-before")
    public List<MarketData> getMinBeforeData(@RequestParam String stockSymbols) {
        long startTime = System.currentTimeMillis();
        List<MarketData> mdList = minBeforeDataFetcher.fetchAndProcessOne(stockSymbols);
        System.out.println("Milliseconds difference: " + (System.currentTimeMillis() - startTime));
        return mdList;
    }

}
