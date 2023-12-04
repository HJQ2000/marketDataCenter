package com.yfd.marketdatacenter.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.model.MarketDataMin;
import com.yfd.marketdatacenter.model.Stock;
import com.yfd.marketdatacenter.controller.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service("oneMinData")
public class HttpQTMarketDataFetcher extends MarketDataFetcher{
    private final SubscriptionService subscriptionService;
    private final WebSocketController webSocketController;

    @Autowired
    public HttpQTMarketDataFetcher(RepositoryService repositoryService, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper,
                                   SubscriptionService subscriptionService, WebSocketController webSocketController) {
        super(repositoryService, redisTemplate, objectMapper);
        this.subscriptionService = subscriptionService;
        this.webSocketController = webSocketController;
    }

    @Override
    public MarketData fetchAndProcessData(String stockIdWithLoc) {
        System.out.println("Fetching--------" + stockIdWithLoc);
        String rawData = fetchDataShortFromHttp(stockIdWithLoc);
        MarketDataMin processData= (MarketDataMin)parseAndProcessShortData(rawData);
        if (processData != null) {
            try{
                webSocketController.sendContMarketDataToWebSocket(stockIdWithLoc, processData);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("webSocket sending error catched");
            }
        }
        saveToRedis(processData);
//        saveToMySQLAsync(processData);
        return processData;
    }

    private void saveToRedis(MarketDataMin marketData) {
//        redisTemplate.opsForValue().set("Test", "Test Redis 1");
        try {
            String value = objectMapper.writeValueAsString(marketData);
            String key = marketData.getStockId();
            redisTemplate.opsForZSet().add(key, value, marketData.getFetchTime());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println("Saving to Redis: " + marketData);
    }

    private void saveToMySQLAsync(MarketDataMin marketData) {
        // 异步存入MySQL
        CompletableFuture.runAsync(() -> {
            repositoryService.save(marketData);
            System.out.println("Saving to MySQL: " + marketData);
        });
    }

//    @Scheduled(cron = "0 * 9-11, 13-15 ? * MON-FRI")
public MarketData getOneForPastMinute(String stockIdWithLoc) {
        long currentTime = System.currentTimeMillis();
        LocalDateTime dateTime = Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println(currentTime + "to date time is: " + dateTime);
        long oneMinuteAgo = currentTime - 60 * 1000;
        Set<String> dataList = redisTemplate.opsForZSet().rangeByScore(stockIdWithLoc, oneMinuteAgo, currentTime);
        double sumPrice = 0;
        long sumDealCount = 0;
        double sumDealValue = 0;
        System.out.println(dataList);
        int count = 0;
        for (String data : dataList) {
            try {
                MarketDataMin marketData = (MarketDataMin)objectMapper.readValue(data, MarketDataMin.class);
                sumPrice += marketData.getCurPrice();
                sumDealValue = marketData.getDealValue();
                sumDealCount = marketData.getDealCount();
                count++;
            } catch (JsonProcessingException e) {
                // 处理异常
                e.printStackTrace();
            }
        }
        if(count <= 0) {
            return new MarketDataMin();
        }
        MarketDataMin marketData = new MarketDataMin(stockIdWithLoc, sumPrice / count,  oneMinuteAgo, LocalDateTime.now());
        marketData.setDealCount(sumDealCount);
        marketData.setDealValue(sumDealValue);
        if (marketData != null) {
            try{
                System.out.println(marketData);

                webSocketController.sendContMarketPerMinDataToWebSocket(stockIdWithLoc, marketData);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("webSocket sending error catched");
            }

//            try {
//                String value = objectMapper.writeValueAsString(marketData);
//                int hour = dateTime.getHour();
//                int minute = dateTime.getMinute();
//                int score = hour * 100 + minute;
//                String key = marketData.getStockId()+"_min";
//                redisTemplate.opsForZSet().add(key, value, score);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
//            System.out.println("Saving to Redis: " + marketData);
        }
        return marketData;
    }
    @Override
    public List<MarketData> fetchAndProcessOne(String stockIdWithLoc) {
        return null;
    }
    @Override
//    @Scheduled(fixedRate = 30000)
    public List<MarketData> fetchAndProcessAll() {
        List<CompletableFuture<MarketData>> futures = subscriptionService.getSubscribedStockCodes().stream()
                .map(symbol -> CompletableFuture.supplyAsync(() -> fetchAndProcessData("sh"+symbol)))
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        List<MarketData> result = allOf.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        ).join();
        System.out.println("Finish batch data fetching at: " + LocalDateTime.now());
        return result;
    }

    @Override
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
    private static final Map<String, String> stockAddressMap = new HashMap<>();
    static {
        stockAddressMap.put("51", "sz");
        stockAddressMap.put("1", "sh");
    }
    private String fetchDataShortFromHttp(String stockIdWithLoc) {
        return fetchDataFromHttp("https://qt.gtimg.cn/q=s_"+stockIdWithLoc, "GBK");
    }

    private MarketData parseAndProcessShortData(String rawData) {
        long startTime = System.currentTimeMillis();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
//        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().atZone(ZoneId.of("Asia/Shanghai")).format(formatter), formatter);
        String content = rawData.split("\"")[1];
        if (content.equals("1")) {
            System.out.println("Error: Bad request");
            return null;
        }
        String[] marketDataArray = content.split("~");
        String stockSymbols = stockAddressMap.get(marketDataArray[0])+marketDataArray[2];
        MarketDataMin md =  new MarketDataMin(stockSymbols, Double.parseDouble(marketDataArray[3]), startTime, LocalDateTime.now());
        try{
            md.setAbsChange(Double.parseDouble(marketDataArray[4]));
            md.setPerChange(Double.parseDouble(marketDataArray[5]));
            md.setDealCount(Long.parseLong(marketDataArray[6]));
            md.setDealValue(Double.parseDouble(marketDataArray[7]));
        } catch(NullPointerException e) {
            System.out.println("Unsuccessfull parsing for: "+stockSymbols);
            e.printStackTrace();
        }

//        if(repositoryService.findStock(stockSymbols).isEmpty()) {
//            Stock stock = new Stock(stockSymbols, marketDataArray[1]);
//            repositoryService.save(stock);
//        }
        return md;
    }


}
