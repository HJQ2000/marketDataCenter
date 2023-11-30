package com.yfd.marketdatacenter.service;

import com.yfd.marketdatacenter.model.MarketData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public interface MarketDataFetcher {
    MarketData fetchAndProcessData(String stockSymbol);
    List<MarketData> fetchAndProcessOne(String stockSymbol);
    List<MarketData> fetchAndProcessAll();

    default String fetchDataFromHttp(String urlString, String charSet) {
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
