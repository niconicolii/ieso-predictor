package com.nico.source;

import com.nico.source.configuration.CityProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

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
            Map<String, String> msg = new HashMap<>();
            msg.put("xmlString", sourceService.getDemandXmlData(iesoXmlUrl));
            msg.put("maxDateTime", sourceService.getMaxDateTimeStr());
            System.out.println("???????????? ran getXmlFromIESO");
            return msg;
        };
    }

    public Supplier<WEathergyMissingMessage> updateWEathergyForYesterday() {
        return () -> {
            return sourceService.constructNewMissingMsgForYesterday();
        };
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkWEathergyCompleteOnStartup() {
        System.out.println("========== Checking WEathergyDB ==========");
        List<WEathergyMissingMessage> missingMessages = sourceService.checkIfWEathergyDatabaseComplete();
        if (missingMessages.isEmpty()) {
            System.out.println("WEathergyDB up-to-date!");
        }
        for (WEathergyMissingMessage msg : missingMessages) {
            streamBridge.send("updateWEathergyInfo", msg);
        }
    }
}
