package com.yfd.marketdatacenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarketdataCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketdataCenterApplication.class, args);
	}

}
