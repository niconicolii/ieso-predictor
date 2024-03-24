package com.nico.openweathermaphistoricalweathergetter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "")
public class CityProperties {

    private Map<String, List<Double>> cities;

    public Map<String, List<Double>> getCities() {
        return cities;
    }

    public void setCities(Map<String, List<Double>> cities) {
        this.cities = cities;
    }
}