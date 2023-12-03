package com.yfd.marketdatacenter.service;

import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SubscriptionService {
    private final ObjectMapper objectMapper;

    private final Set<String> allStockCodesSet = new HashSet<>();
    private final Map<String, String> allStockCodesNameMap = new HashMap<>();
    private Set<String> subscribedStockCodes;

    private String subscriptionInfoFilePath = "subscribeInfo.json";
    private String allStockInfoFilePathSh = "sh.txt";


    public SubscriptionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.subscribedStockCodes = loadSubscriptionList();
    }

    public void subscribe(String stockCode) {
        subscribedStockCodes.add(stockCode);
    }

    public void unsubscribe(String stockCode) {
        subscribedStockCodes.remove(stockCode);
    }

    public Set<String> getSubscribedStockCodes() {
        return subscribedStockCodes;
    }

    private Set<String> loadSubscriptionList() {
        try {
            File file = new File(subscriptionInfoFilePath);
            if (file.exists()) {
                Set<String> subscriptionInfo = objectMapper.readValue(file, Set.class);
                System.out.println("Subsription info loaded: "+ subscriptionInfo);
                return subscriptionInfo;
            }
        } catch (IOException e) {
            e.printStackTrace(); // 处理异常
        }
        return new HashSet<>();
    }

    @PreDestroy
    private void saveSubscriptionList() {
        System.out.println("Destroyed and save to file: " + subscribedStockCodes);
        try {
            File file = new File(subscriptionInfoFilePath);
            Files.write(file.toPath(), objectMapper.writeValueAsBytes(subscribedStockCodes));
//            objectMapper.writeValue(file, subscribedStockCodes);
        } catch (IOException e) {
            e.printStackTrace(); // 处理异常
        }
    }

    private static String extractContentWithPattern(String input, String pat) {
        Pattern pattern = Pattern.compile(pat);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    public List<String> getAllStockCodes() {
        // 从配置文件中读取股票代码
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(allStockInfoFilePathSh).getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String stockSymbol = extractContentWithPattern(line, "\\((\\d{6})\\)");
                String stockName = extractContentWithPattern(line, "^(.*?)\\(");
                if (stockSymbol != null) {
                    allStockCodesSet.add(stockSymbol);
                    allStockCodesNameMap.put(stockSymbol, stockName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> allStockCodesList = new ArrayList<>(allStockCodesSet);
        return allStockCodesList;
    }
}
