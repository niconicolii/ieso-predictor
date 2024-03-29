package com.nico.organizeHistoricalData;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.nico.organizeHistoricalData.CityProperties")
public class OrginizeHistoricalDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrginizeHistoricalDataApplication.class, args);
    }
}
