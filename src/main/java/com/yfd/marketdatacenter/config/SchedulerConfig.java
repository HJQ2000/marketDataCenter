package com.yfd.marketdatacenter.config;

import com.yfd.marketdatacenter.service.HttpQTMarketDataFetcher;
import com.yfd.marketdatacenter.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.LocalTime;
import java.util.Set;

@Configuration
@EnableScheduling

public class SchedulerConfig {

    private final SubscriptionService subscriptionService;
    private final HttpQTMarketDataFetcher marketDataFetcher;


    @Autowired
    public SchedulerConfig(SubscriptionService subscriptionService, HttpQTMarketDataFetcher marketDataFetcher) {
        this.subscriptionService = subscriptionService;
        this.marketDataFetcher = marketDataFetcher;
    }

    private boolean checkTime() {
        LocalTime currentTime = LocalTime.now();
        if ((currentTime.isAfter(LocalTime.of(13, 00)) && currentTime.isBefore(LocalTime.of(15, 01)))
                        || (currentTime.isAfter(LocalTime.of(9, 30)) && currentTime.isBefore(LocalTime.of(11, 31)))) {
            System.out.println("Executing scheduled task at " + currentTime);
            return true;
        } else {
            System.out.println("Skipped scheduled task at " + currentTime);
            return false;
        }

    }
//    @Scheduled(cron="*/20 * * * * *")
    @Scheduled(cron = "*/5 * 9-15 ? * MON-FRI")
    public void scheduledFetch() {
        if (checkTime()) {
//            Set<String> subscriptions = subscriptionService.getSubscribedStockCodes();
//            for (String stockCode : subscriptions) {
//                marketDataFetcher.fetchAndProcessData(stockCode);
//                System.out.println("Scheduled fetching for stock: " + stockCode);
//            }
        marketDataFetcher.fetchAndProcessSubscribed();
        }
    }

    @Scheduled(cron = "0 * 9-15 ? * MON-FRI")
    public void scheduledCalculate() {
        Set<String> subscriptions = subscriptionService.getSubscribedStockCodes();
        if(checkTime()) {
            for (String stockCode : subscriptions) {
                marketDataFetcher.getOneForPastMinute(stockCode);
                System.out.println("Scheduled Per minute Calculation for stock: " + stockCode);
            }
        }
    }

}
//public class SchedulerConfig implements SchedulingConfigurer {
//
//    private final SubscriptionService subscriptionService;
//    private final HttpQTMarketDataFetcher marketDataFetcher;
//    @Autowired
//    public SchedulerConfig(SubscriptionService subscriptionService, HttpQTMarketDataFetcher marketDataFetcher) {
//        this.subscriptionService = subscriptionService;
//        this.marketDataFetcher = marketDataFetcher;
//    }
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
////        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
////        taskScheduler.setPoolSize(10); // 设置线程池大小
////        taskScheduler.initialize();
////        taskRegistrar.setTaskScheduler(taskScheduler);
//
//        System.out.println("Scheduler config: " + subscriptionService.getSubscribedStockCodes());
//
//        taskRegistrar.addCronTask(() -> marketDataFetcher.fetchAndProcessData("sh600519"), "*/10 * * * * *");
////        subscriptionService.getSubscribedStockCodes().forEach(stockCode -> {
////            System.out.println("get Subscription: " + stockCode);
////            taskRegistrar.addCronTask(() -> marketDataFetcher.fetchAndProcessData(stockCode), "*/10 * * * * *"); //"*/10 * 9-11,13-15 ? * MON-FRI"
////        });
//////
////        // 为 getMeanPricePrevMin(stockID) 创建调度
////        subscriptionService.getSubscribedStockCodes().forEach(stockCode -> {
////            taskRegistrar.addCronTask(() -> marketDataFetcher.getOneForPastMinute(stockCode), "0 * * * * *"); //"0 * 9-11, 13-15 ? * MON-FRI"
////        });
//    }
//
//}
