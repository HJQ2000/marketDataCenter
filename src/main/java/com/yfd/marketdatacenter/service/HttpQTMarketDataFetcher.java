package com.yfd.marketdatacenter.service;
import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.model.MarketDataMin;
import com.yfd.marketdatacenter.repository.MinDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service("oneMinData")
public class HttpQTMarketDataFetcher implements MarketDataFetcher{

    private final List<String> stockList;
    private final MinDataRepository rep;

    @Autowired
    public HttpQTMarketDataFetcher(MinDataRepository rep) {
        this.rep = rep;
        this.stockList = new StockSymbols().getAllStockSymbols();
    }
    @Override
    public MarketData fetchAndProcessData(String stockIdWithLoc) {
        String rawData = fetchDataShortFromHttp(stockIdWithLoc);
        MarketData processData= parseAndProcessShortData(rawData);
//        System.out.println("Fetching data at: " + LocalDateTime.now());
//        rep.save((MarketDataMin) proce ssData);
        return processData;
    }
    @Override
    public List<MarketData> fetchAndProcessOne(String stockIdWithLoc) {
        return null;
    }
    @Override
//    @Scheduled(fixedRate = 30000)
    public List<MarketData> fetchAndProcessAll() {
        List<CompletableFuture<MarketData>> futures = stockList.subList(1000, 1010).stream()
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
            return new MarketDataMin();
        }
        String[] marketDataArray = content.split("~");
        MarketDataMin md =  new MarketDataMin(stockAddressMap.get(marketDataArray[0])+marketDataArray[2], Double.parseDouble(marketDataArray[3]), startTime);
        md.setAbsChange(Double.parseDouble(marketDataArray[4]));
        md.setPerChange(Double.parseDouble(marketDataArray[5]));
        md.setDealCount(Long.parseLong(marketDataArray[6]));
        md.setDealValue(Double.parseDouble(marketDataArray[7]));
        return md;
    }
}
