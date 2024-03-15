package com.nico.processor;

import com.nico.processor.dataClasses.DemandData;
import com.nico.processor.dataClasses.DemandMultidaysData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Configuration
public class ParseXmlConfiguration {
    private final ParseXmlService parseXmlService;

    @Autowired
    public ParseXmlConfiguration(ParseXmlService parseXmlService) {
        this.parseXmlService = parseXmlService;
    }

    @Bean
    public Function<String, List<Message<DemandData>>> parseXml() {
        return xmlData -> {
            List<Message<DemandData>> messages;
            try {
                DemandMultidaysData dmd = parseXmlService.parseXmlToObj(xmlData);
                messages = parseXmlService.getMessageList(dmd);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return messages;
        };
    }
}
