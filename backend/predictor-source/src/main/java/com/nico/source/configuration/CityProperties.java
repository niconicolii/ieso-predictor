package com.nico.source.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "")
public class CityProperties {
    private Map<String, Map<String, Number>> cities;
    public Map<String, Map<String, Number>> getCities() {
        return cities;
    }
    public void setCities(Map<String, Map<String, Number>> cities) {
        this.cities = cities;
    }
}