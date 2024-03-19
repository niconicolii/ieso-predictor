package com.nico.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class SaveToDBConfiguration {
    @Autowired
    private final SaveToDBService service;

    public SaveToDBConfiguration(SaveToDBService service) {
        this.service = service;
    }


    @Bean
    public Consumer<Message<DemandData>> saveToDB() {
        return message -> {
            // TODO: remove later
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            service.saveToRepository(message.getPayload());
        };
    }
}
