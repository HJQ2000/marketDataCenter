package com.yfd.marketdatacenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfd.marketdatacenter.controller.WebSocketController;
import com.yfd.marketdatacenter.model.MarketData;
import com.yfd.marketdatacenter.model.MarketDataMin;
import com.yfd.marketdatacenter.model.Stock;
import com.yfd.marketdatacenter.repository.MinDataRepository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.JSONObject;
import org.json.JSONTokener;
@Service("minBeforeData")
public class HttpQTMinBeforeDataFetcher extends MarketDataFetcher {

    @Autowired
    public HttpQTMinBeforeDataFetcher(RepositoryService repositoryService, RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        super(repositoryService, redisTemplate, objectMapper);
    }
    @Override
    public MarketData fetchAndProcessData(String stockSymbol) {
        return null;
    }

    @Override
    public List<MarketData> fetchAndProcessOne(String stockSymbol) {
//        LocalDateTime dateTime = LocalDateTime.now();
//        int hour = dateTime.getHour();
//        int minute = dateTime.getMinute();
//        Set<String> dataList = redisTemplate.opsForZSet().rangeByScore(stockSymbol+"_min"+dateTime.getDayOfYear(), 930, hour * 100 + minute);
//        List<MarketData> result = new ArrayList<>();
//        for (String data : dataList) {
//            try {
//                MarketDataMin marketData = (MarketDataMin)objectMapper.readValue(data, MarketDataMin.class);
//                result.add(marketData);
//            } catch (JsonProcessingException e) {
//                // 处理异常
//                e.printStackTrace();
//            }
//        }

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

        MarketDataMin start = new MarketDataMin();
        double sumPrice = 0;
        int count = 0;

        for(Object ele:jsonArray) {
            String[] info = ele.toString().split(" ");
            LocalDateTime dateTime = LocalDateTime.parse(date+info[0], formatter);
            MarketDataMin md = new MarketDataMin(stockIdWithLoc, Double.parseDouble(info[1]), fetchTime, dateTime);
            md.setDealValue(Double.parseDouble(info[3]));
            md.setDealCount(Long.parseLong(info[2]));
            minInfo.add(md);
            sumPrice += md.getCurPrice();
            count += 1;
            if (info[0].equals("0930")) {
                md.setMeanPrice(sumPrice / count);
                start = md;
            } else {
                md.setAbsChange(md.getCurPrice() - start.getCurPrice());
                md.setPerChange(md.getAbsChange() / start.getCurPrice());
                md.setMeanPrice(sumPrice / count);
            }

            List<MarketDataMin> existing = repositoryService.findByStockIdAndTimeStamp(md.getStockId(), md.getTimeStampChina());
            if(existing == null || existing.size()==0){
                repositoryService.save(md);
            }
        }
        return minInfo;
    }
}
