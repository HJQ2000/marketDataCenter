package com.yfd.marketdatacenter.handler;

import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.service.HttpQTMinBeforeDataFetcher;
import com.yfd.marketdatacenter.service.MarketDataFetcher;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

public class StockWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established for session: " + session.getId());
    }


    private MarketDataFetcher marketDataFetcher = new HttpQTMinBeforeDataFetcher();
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String stockSymbol = message.getPayload();
        List<MarketData> mdList = marketDataFetcher.fetchAndProcessOne(stockSymbol);

        System.out.println("Received message '" + stockSymbol + "' from session: " + session.getId());
//        String responseMessage = "Current price for " + stockSymbol + " is" + md.getCurPrice() + " at Time: " + md.getFetchTime();
        String responseMessage = "Current length for " + stockSymbol + " is" + mdList.size();
        session.sendMessage(new TextMessage(responseMessage));
    }
}
