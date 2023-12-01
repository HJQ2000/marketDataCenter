package com.yfd.marketdatacenter.scheduler;

import com.yfd.marketdatacenter.controller.WebSocketController;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MarketDataMinScheduler {
    private final WebSocketController webSocketController;

    public MarketDataMinScheduler(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;

    }

//    @Scheduled(fixedRate = 30000) // 每隔30秒执行一次
//    public void pushStockDataInitialize() {
//
//        String stockCode = "sh600519"; // 这里可以替换成实际的股票数据获取逻辑
//        webSocketController.handleStockRequestInitialize(stockCode);
//    }
}
