package com.nico.organizeHistoricalData;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "")
public class CityProperties {

//    private Map<String, List<Double>> cities;
    private Map<String, Map<String, Number>> cities;

//    public Map<String, List<Double>> getCities() {
//        return cities;
//    }
    public Map<String, Map<String, Number>> getCities() {
        return cities;
    }

//    public void setCities(Map<String, List<Double>> cities) {
//        this.cities = cities;
//    }
    public void setCities(Map<String, Map<String, Number>> cities) {
        this.cities = cities;
    }
}