package com.nico.sink;

import com.nico.sink.dataClasses.DemandData;
import com.nico.sink.dataClasses.ForecastData;
import com.nico.sink.dataClasses.WEathergyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.List;
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
            service.saveToRepository(message.getPayload());
        };
    }

    @Bean
    public Consumer<Message<WEathergyData>> saveWEathergyToDB() {
        return message -> {
            service.saveWEathergyToDB(message.getPayload());
        };
    }

    @Bean
    public Consumer<List<ForecastData>> saveForecastToDB() {
        return forecastList -> {
            System.out.println("[saveForecastToDB] received: " + forecastList);
            service.saveForecastToDB(forecastList);
        };
    }
}
