package com.yfd.marketdatacenter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfd.marketdatacenter.controller.WebSocketController;
import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.model.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public abstract class MarketDataFetcher {

    protected final RepositoryService repositoryService;
    protected final RedisTemplate<String, String> redisTemplate;
    protected final ObjectMapper objectMapper;

    @Autowired
    public MarketDataFetcher(RepositoryService repositoryService, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.repositoryService = repositoryService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public abstract MarketData fetchAndProcessData(String stockSymbol);
    public abstract  List<MarketData> fetchAndProcessOne(String stockSymbol);
    public abstract List<MarketData> fetchAndProcessAll();

    public String getName(String stockSymbol){
        String stockName = redisTemplate.opsForValue().get(stockSymbol + "_name");
        if (stockName == null) {
            Optional<Stock> stock = repositoryService.findStock(stockSymbol);
            if (!stock.isEmpty()) {
                stockName = stock.get().getStockName();
                redisTemplate.opsForValue().set(stockSymbol + "_name", stockName);
            }
        }
        return stockName;
    }

    public String fetchDataFromHttp(String urlString, String charSet) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader readIn = new BufferedReader(new InputStreamReader(connection.getInputStream(), charSet));
                StringBuilder response = new StringBuilder();
                String input;
                while ((input = readIn.readLine()) != null) {
                    response.append(input);
                }
                connection.disconnect();
                return response.toString();
            } else {
                return "HTTP request failed with response code: " + responseCode;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred during HTTP request.";
        }
    }

}
