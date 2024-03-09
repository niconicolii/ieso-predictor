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
    private final SaveToDBService service;

    @Autowired
    public SaveToDBConfiguration(SaveToDBService service) {

        this.service = service;
    }

    @Bean
    public Consumer<Message<String>> saveToDB() {
        return serializedMessage -> {
            DemandData data;
            try {
                data = service.deserializeDemandData(serializedMessage.getPayload());
//                System.out.println("=========================================================\n" +
//                        "Timestamp: " + data.getTimestamp() + "; Value: " + data.getValue() +
//                        "\n json: " + serializedMessage.getPayload());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            service.saveToRepository(data);
        };
    }
}
