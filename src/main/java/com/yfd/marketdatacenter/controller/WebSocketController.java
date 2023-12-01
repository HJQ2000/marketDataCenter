package com.yfd.marketdatacenter.controller;

import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.model.MarketDataMin;
import com.yfd.marketdatacenter.service.HttpQTMinBeforeDataFetcher;
import com.yfd.marketdatacenter.service.MarketDataFetcher;
import com.yfd.marketdatacenter.service.StockSymbols;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class WebSocketController {
    private List<MarketData> stockData = new ArrayList<>();
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    @MessageMapping("/stock")
    @SendTo("/topic/stockData")
    public List<MarketData> handleStockRequestInitialize(String stockSymbol) {
        System.out.println("Received stock code: " + stockSymbol);
        MarketDataFetcher marketDataFetcher = new HttpQTMinBeforeDataFetcher();
        stockData = marketDataFetcher.fetchAndProcessOne(stockSymbol).subList(0, 20);
        simulateAdd(stockSymbol);
        return stockData;
    }
    @Autowired
    @Qualifier("oneMinData")
    private MarketDataFetcher marketDataFetcherCur;
    private void simulateAdd(String stockSymbol) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Scheduled rerun yes!");
                MarketDataMin newMd = (MarketDataMin)marketDataFetcherCur.fetchAndProcessData(stockSymbol);
                System.out.println(newMd.getStockId() + " price is: " + newMd.getCurPrice() + " at time " + newMd.getTimeStampChina());
                stockData.add(newMd);
                messagingTemplate.convertAndSend("/topic/stockData/" + newMd.getStockId(), newMd);
            }
        }, 0, 10000);
    }


}
