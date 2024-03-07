package com.nico.sink;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class SaveToDBConfiguration {

    @Bean
    public Consumer<String> saveToDB() {
        return timeDemand -> {
            System.out.println(timeDemand);
        };
    }
}
