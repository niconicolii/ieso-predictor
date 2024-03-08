package com.nico.sink;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.Arrays;
import java.util.function.Consumer;

@Configuration
public class SaveToDBConfiguration {
    private final SaveToDBService saveToDBService;

    @Autowired
    public SaveToDBConfiguration(SaveToDBService saveToDBService) {
        this.saveToDBService = saveToDBService;
    }

    @Bean
    public Consumer<Message<String>> saveToDB() {
        return serializedMessage -> {
            try {
                DemandData data = saveToDBService.deserializeDemandData(serializedMessage.getPayload());
                System.out.println("=========================================================\n" +
                        "Timestamp: " + data.getTimestamp() + "; Value: " + data.getValue());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
