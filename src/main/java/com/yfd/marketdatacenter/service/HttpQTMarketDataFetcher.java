package com.yfd.marketdatacenter.service;
import com.yfd.marketdatacenter.model.MarketData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
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

    private List<String> stockList = new StockSymbols().getAllStockSymbols();
    @Override
    @Scheduled(fixedRate = 30000)
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
        try {
            URL url = new URL(
                    "https://qt.gtimg.cn/q=s_" + stockIdWithLoc);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader readIn = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));
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

    private MarketData parseAndProcessShortData(String rawData) {
        long startTime = System.currentTimeMillis();
        String content = rawData.split("\"")[1];
        if (content.equals("1")) {
            System.out.println("Error: Bad request");
            return new MarketData();
        }
        String[] marketDataArray = content.split("~");

        return new MarketData(stockAddressMap.get(marketDataArray[0]), marketDataArray[1], marketDataArray[2], Double.parseDouble(marketDataArray[3]), startTime);
    }
}
