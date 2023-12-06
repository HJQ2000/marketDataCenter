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
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;


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
//        System.out.println("Fetching:" + stockIdWithLoc);
        String rawData = fetchDataShortFromHttp(stockIdWithLoc);
        MarketDataMin processData= (MarketDataMin)parseAndProcessShortData(rawData);
        if (processData != null) {
            try{
                webSocketController.sendContMarketDataToWebSocket(stockIdWithLoc, processData);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("webSocket sending error catched");
            }
            saveToRedis(processData, processData.getStockId(), processData.getFetchTime());
            saveToMySQLAsync(processData);
        } else {
            System.out.println("Bad request for " + stockIdWithLoc);
        }
        return processData;
    }

    private void saveToRedis(MarketDataMin marketData, String key, long score) {
//        redisTemplate.opsForValue().set("Test", "Test Redis 1");
        try {
            String value = objectMapper.writeValueAsString(marketData);
            redisTemplate.opsForZSet().add(key, value, score);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
//        System.out.println("Saving to Redis: " + marketData);
    }

    private void saveToMySQLAsync(MarketDataMin marketData) {
        // 异步存入MySQL
        CompletableFuture.runAsync(() -> {
            repositoryService.save(marketData);
            System.out.println("Saving to MySQL: " + marketData);
        });
    }



    private double getPastMeanPrice(String stockIdWithLoc, long current) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalTime startTimeL = LocalTime.of(9, 30);
        LocalDateTime startDateTime = LocalDateTime.of(startDate.toLocalDate(), startTimeL);

        long startTime = startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        Set<String> dataSet = redisTemplate.opsForZSet().rangeByScore(stockIdWithLoc, startTime, current);
        int count = dataSet.size();
        double sumPrice = 0;
        for (String data:dataSet) {
            try {
                MarketDataMin lastData = objectMapper.readValue(data, MarketDataMin.class);
                sumPrice += lastData.getCurPrice();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if(count > 0) {
            return sumPrice / count;
        }
        return 0;
    }

    public MarketData getOneForPastMinute(String stockIdWithLoc) {
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60 * 1000;
        LocalDateTime nowTime = Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault()).toLocalDateTime();//ZoneId.of("Asia/Shanghai")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime toSaveNow = LocalDateTime.parse(nowTime.format(formatter), formatter);

        Set<String> dataSet = redisTemplate.opsForZSet().rangeByScore(stockIdWithLoc, oneMinuteAgo, currentTime);
        int count = dataSet.size();
        List<String> dataList = new ArrayList<>(dataSet);

        String prev = redisTemplate.opsForValue().get(stockIdWithLoc+"_day_"+nowTime.getDayOfYear()+"_time_"+930);
        MarketDataMin startData = new MarketDataMin();
        if (prev != null) {
            try {
                startData = objectMapper.readValue(prev, MarketDataMin.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            try{
                startData = repositoryService.findByStockIdAndTimeStamp(stockIdWithLoc,
                        LocalDateTime.parse(nowTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "0930",formatter)
                ).get(0);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        if (count == 0) {
            return new MarketDataMin();
        }

        double price = 0;
        long dealCount = 0;
        double dealValue = 0;
        double perChange = 0;
        double absChange = 0;

        if (count >= 1) {
            try {
                MarketDataMin lastData = objectMapper.readValue(dataList.get(count-1), MarketDataMin.class);
                price = lastData.getCurPrice();
                dealCount = lastData.getDealCount();
                dealValue = lastData.getDealValue();
                absChange = price - startData.getCurPrice();
                perChange = absChange / startData.getCurPrice();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        MarketDataMin marketData = new MarketDataMin(stockIdWithLoc, price,  currentTime, toSaveNow);
        marketData.setDealCount(dealCount);
        marketData.setDealValue(dealValue);
        marketData.setPerChange(perChange);
        marketData.setAbsChange(absChange);
        marketData.setMeanPrice(getPastMeanPrice(stockIdWithLoc, currentTime));

        try{
            webSocketController.sendContMarketPerMinDataToWebSocket(stockIdWithLoc, marketData);
            String value = objectMapper.writeValueAsString(marketData);
            redisTemplate.opsForValue().set(stockIdWithLoc+"_day_"+nowTime.getDayOfYear()+"_time_"+(nowTime.getHour()*100+nowTime.getMinute()), value);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("webSocket sending error catched");
        }
        saveToMySQLAsync(marketData);
        return marketData;
    }
    @Override
    public List<MarketData> fetchAndProcessOne(String stockIdWithLoc) {
        return null;
    }
    @Override
//    @Scheduled(fixedRate = 30000)
    public List<MarketData> fetchAndProcessAll() {
        System.out.println("Start batch all data fetching at: " + LocalDateTime.now());
        List<CompletableFuture<MarketData>> futures = subscriptionService.getAllStockCodes().stream()
                .map(symbol -> CompletableFuture.supplyAsync(() -> fetchAndProcessData("sh"+symbol)))
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        List<MarketData> result = allOf.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        ).join();
        System.out.println("Finish batch all data fetching at: " + LocalDateTime.now());
        return result;
    }

    public List<MarketData> fetchAndProcessSubscribed() {
        List<CompletableFuture<MarketData>> futures = subscriptionService.getSubscribedStockCodes().stream()
                .map(symbol -> CompletableFuture.supplyAsync(() -> fetchAndProcessData(symbol)))
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        List<MarketData> result = allOf.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        ).join();
        System.out.println("Finish batch subscribed data fetching at: " + LocalDateTime.now());
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

        if(repositoryService.findStock(stockSymbols).isEmpty()) {
            Stock stock = new Stock(stockSymbols, marketDataArray[1]);
            repositoryService.save(stock);
        }
        return md;
    }


}
