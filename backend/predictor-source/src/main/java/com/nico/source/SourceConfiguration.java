package com.nico.source;

import com.nico.source.configuration.CityProperties;
import com.nico.source.dataClasses.WEathergyMissingMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@EnableConfigurationProperties(CityProperties.class)
public class SourceConfiguration {

    @Value("${ieso.source.url}")
    private String iesoXmlUrl;

    private final SourceService sourceService;
    private final StreamBridge streamBridge;

    @Autowired
    public SourceConfiguration(SourceService sourceService, StreamBridge streamBridge) {
        this.sourceService = sourceService;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Supplier<Map<String, String>> getXMLFromIESO() {
        return () -> {
            System.out.println("========== Getting 5-min Demand from IESO ==========");
            Map<String, String> msg = new HashMap<>();
            msg.put("xmlString", sourceService.getDemandXmlData(iesoXmlUrl));
            msg.put("maxDateTime", sourceService.getMaxDateTimeStr());
            return msg;
        };
    }

    public Supplier<WEathergyMissingMessage> updateWEathergyForYesterday() {
        return sourceService::constructNewMissingMsgForYesterday;
    }

    // steps on startup:
    //      1. check if WEathergyDB has all required history data
    //      2. get latest weather forecast
    @EventListener(ApplicationReadyEvent.class)
    public void checkWEathergyCompleteOnStartup() {
        System.out.println("========== Startup Check on WEathergyDB ==========");
        List<WEathergyMissingMessage> missingMessages = sourceService.checkIfWEathergyDatabaseComplete();
        if (missingMessages.isEmpty()) {
            System.out.println("WEathergyDB up-to-date!");
        }
        for (WEathergyMissingMessage msg : missingMessages) {
            streamBridge.send("updateWEathergyInfo", msg);
        }
        System.out.println("========== Startup Retrieval of Forecast ==========");
        streamBridge.send("updateCurrHourWeather", sourceService.getForecastUrls());
    }

    @Bean
    public Supplier<Map<String, String>> getNewWeatherInfo() {
        // two steps:
        // 1) send missingMsg to create new WEathergyData for curr hour

        // 2) send urls to OpenWeatherAPI to get weather forecast
        return () -> {
            System.out.println("========== Hourly WEathergy & Forecast Update ==========");
            WEathergyMissingMessage msg = sourceService.constructNewMissingMsgForCurrHour();
            return sourceService.getForecastUrls();
        };
    }
}
