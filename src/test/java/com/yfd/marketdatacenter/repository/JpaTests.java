package com.yfd.marketdatacenter.repository;

import com.yfd.marketdatacenter.model.MarketDataMin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootTest
public class JpaTests {
    @Autowired
    private MinDataRepository repository;

    @Test
    public void findByStockId() {
        List<MarketDataMin> stockList = repository.findByStockId("sh600519");
        System.out.println(stockList);
        for(MarketDataMin md:stockList) {
            System.out.println(md.getStockId());
            System.out.println(md.getCurPrice());
        }
    }

    @Test
    public void findByStockIdAndTimeStamp() {
        List<MarketDataMin> stockList = repository.findByStockIdAndTimeStamp("sh600519", LocalDateTime.parse("202312021006", DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        System.out.println(stockList);
        for(MarketDataMin md:stockList) {
            System.out.println(md.getStockId());
            System.out.println(md.getCurPrice());
        }

        List<MarketDataMin> invalidList = repository.findByStockIdAndTimeStamp("sh60519", LocalDateTime.parse("202312021006", DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        System.out.println(invalidList);
        System.out.println(invalidList==null);
        System.out.println(invalidList.size()==0);

    }

    @Test
    public void save() {
        MarketDataMin md = new MarketDataMin("sh600519", Double.parseDouble("1760"), System.currentTimeMillis(), LocalDateTime.now());
        repository.save(md);
    }
}
