package com.yfd.marketdatacenter.controller;

import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.model.MarketDataMin;
import com.yfd.marketdatacenter.service.MarketDataFetcher;
import com.yfd.marketdatacenter.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class WebSocketController {
    private List<MarketData> stockData = new ArrayList<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final SubscriptionService subscriptionService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, SubscriptionService subscriptionService) {
        this.messagingTemplate = messagingTemplate;
        this.subscriptionService = subscriptionService;
    }

    @Autowired
    @Qualifier("minBeforeData")
    private MarketDataFetcher marketDataFetcherMinBefore;
    @MessageMapping("/stock")
    @SendTo("/topic/initialStockData")
    public List<MarketData> handleStockRequestInitialize(String stockSymbol) {
        System.out.println("Received stock code: " + stockSymbol);
        subscriptionService.subscribe(stockSymbol);
        stockData = marketDataFetcherMinBefore.fetchAndProcessOne(stockSymbol).subList(0, 20);
//        simulateAdd(stockSymbol);
        return stockData;
    }

    public void sendInitialMarketDataToWebSocket(String stockSymbol, List<MarketData> mdList) {
        String targetDestination = "/topic/initialStockData/";
        messagingTemplate.convertAndSend(targetDestination, mdList);
    }

    public void sendContMarketDataToWebSocket(String stockSymbol, MarketData marketData) {
        String targetDestination = "/topic/updatedStockData/" + stockSymbol;
        messagingTemplate.convertAndSend(targetDestination, marketData);
    }

//    @Autowired
//    @Qualifier("oneMinData")
//    private MarketDataFetcher marketDataFetcherCur;
//    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//    private void simulateAdd(String stockSymbol) {
//        scheduler.scheduleAtFixedRate(() -> {
//            System.out.println("Scheduled rerun yes!");
//            MarketDataMin newMd = (MarketDataMin) marketDataFetcherCur.fetchAndProcessData(stockSymbol);
//            System.out.println(newMd.getStockId() + " price is: " + newMd.getCurPrice() + " at time " + newMd.getTimeStampChina());
//            stockData.add(newMd);
//            System.out.println("/topic/updatedStockData/" + stockSymbol);
//            messagingTemplate.convertAndSend("/topic/updatedStockData/"+stockSymbol, newMd);
//        }, 0, 10, TimeUnit.SECONDS);
//    }

}
