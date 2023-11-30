package com.yfd.marketdatacenter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StockSymbols {

    private static String extractContentInBrackets(String input) {
        Pattern pattern = Pattern.compile("\\((\\d{6})\\)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    public List<String> getAllStockSymbols() {
        // 从配置文件中读取股票代码
        List<String> stockList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("sh.txt").getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String stockSymbols = extractContentInBrackets(line);
                if (stockSymbols != null) {
                    stockList.add(stockSymbols);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stockList;
    }
}
