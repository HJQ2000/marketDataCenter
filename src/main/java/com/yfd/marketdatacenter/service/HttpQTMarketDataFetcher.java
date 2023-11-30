package com.yfd.marketdatacenter.service;
import com.yfd.marketdatacenter.model.MarketData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class HttpQTMarketDataFetcher implements MarketDataFetcher{
    @Override
    public MarketData fetchAndProcessData(String stockIdWithLoc) {
        String rawData = fetchDataShortFromHttp(stockIdWithLoc);
        MarketData processData= parseAndProcessShortData(rawData);
//        System.out.println("Fetching data at: " + LocalDateTime.now());
        return processData;
    }
    @Override
    public List<MarketData> fetchAndProcessOne(String stockIdWithLoc) {
        return null;
    }
    private List<String> stockList = new StockSymbols().getAllStockSymbols();
    @Override
//    @Scheduled(fixedRate = 30000)
    public List<MarketData> fetchAndProcessAll() {
        List<CompletableFuture<MarketData>> futures = stockList.subList(1000, 1100).stream()
                .map(symbol -> CompletableFuture.supplyAsync(() -> fetchAndProcessData("sh"+symbol)))
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        List<MarketData> result = allOf.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        ).join();
        System.out.println("Fetching data at: " + LocalDateTime.now());
        return result;
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
        String content = rawData.split("\"")[1];
        if (content.equals("1")) {
            System.out.println("Error: Bad request");
            return new MarketData();
        }
        String[] marketDataArray = content.split("~");
        MarketData md =  new MarketData(stockAddressMap.get(marketDataArray[0])+marketDataArray[2], Double.parseDouble(marketDataArray[3]), startTime);
        md.setStockName(marketDataArray[1]);
        return md;
    }
}
