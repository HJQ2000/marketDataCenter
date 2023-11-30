package com.yfd.marketdatacenter.service;

import com.yfd.marketdatacenter.model.MarketData;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONTokener;
@Service
public class HttpQTMinBeforeDataFetcher implements MarketDataFetcher {
    @Override
    public MarketData fetchAndProcessData(String stockSymbol) {
        return null;
    }

    @Override
    public List<MarketData> fetchAndProcessOne(String stockSymbol) {
        return fetchAndParseDataFromHttp(stockSymbol);
    }
    @Override
    public List<MarketData> fetchAndProcessAll() {
        return null;
    }

    private List<MarketData> fetchAndParseDataFromHttp(String stockIdWithLoc) {
        long fetchTime = System.currentTimeMillis();
        String response = fetchDataFromHttp("https://web.ifzq.gtimg.cn/appstock/app/minute/query?code="+stockIdWithLoc, "GBK");
        JSONTokener tokener = new JSONTokener(response.toString());
        JSONObject finalResult = new JSONObject(tokener);
        String date = finalResult.getJSONObject("data").getJSONObject(stockIdWithLoc).getJSONObject("data").getString("date");
        JSONArray jsonArray= finalResult.getJSONObject("data").getJSONObject(stockIdWithLoc).getJSONObject("data").getJSONArray("data");
        System.out.println(date);
        System.out.println(jsonArray);
        List<MarketData> minInfo = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        for(Object ele:jsonArray) {
            String[] info = ele.toString().split(" ");
            LocalDateTime dateTime = LocalDateTime.parse(date+info[0], formatter);
            MarketData md = new MarketData(stockIdWithLoc, Double.parseDouble(info[1]), fetchTime);
            md.setStockDealValue(Double.parseDouble(info[3]));
            md.setStockDealCount(Long.parseLong(info[2]));
            minInfo.add(md);
        }
        return minInfo;
    }
}