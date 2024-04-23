package com.nico.processor;

import com.nico.processor.dataClasses.*;
import com.nico.processor.service.ProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class PredictorProcessorConfiguration {
    private final ProcessorService service;

    @Autowired
    public PredictorProcessorConfiguration(ProcessorService service) {
        this.service = service;
    }

    @Bean
    public Function<Map<String, String>, List<Message<DemandData>>> parseXml() {
        return msg -> {
            List<Message<DemandData>> outputMessages;
            LocalDateTime maxDateTime = LocalDateTime.parse(msg.get("maxDateTime"));
            try {
                DemandMultidaysData dmd = service.parseXmlToObj(msg.get("xmlString"));
                outputMessages = service.getMessageList(dmd, maxDateTime);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return outputMessages;
        };
    }

    @Bean
    public Function<WEathergyMissingMessage, List<Message<WEathergyData>>> createMissingWEathergy() {
        return missingMsg -> {
            System.out.println(missingMsg);
            List<WEathergyData> missingWEathergyData;
            try {
                missingWEathergyData = service.createMissingWEathergyData(missingMsg);
                service.fillWithDemandData(missingWEathergyData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return service.wrapMessage(missingWEathergyData);
        };
    }

    @Bean
    public Function<Map<String, String>, List<ForecastData>> updateCurrHourWeather() {
        return urls -> {
            try {
                return service.getForecastFromApi(urls);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
