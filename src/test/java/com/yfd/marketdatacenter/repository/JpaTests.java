package com.yfd.marketdatacenter.repository;

import com.yfd.marketdatacenter.model.MarketDataMin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.awt.print.Pageable;
import java.util.List;

@SpringBootTest
public class JpaTests {
    @Autowired
    private minDataRepository repository;

    @Test
    public void getMinDataGivenStock() {
        List<MarketDataMin> stockList = repository.getMinDataGivenStock("sh600519");
        System.out.println(stockList);
        for(MarketDataMin md:stockList) {
            System.out.println(md.getStockId());
            System.out.println(md.getCurPrice());
        }
    }

    @Test
    public void save() {
        MarketDataMin md = new MarketDataMin("sh600519", Double.parseDouble("1760"), System.currentTimeMillis());
        repository.save(md);
    }
}
