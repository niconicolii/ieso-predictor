package com.nico.openweathermaphistoricalweathergetter;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CityProperties.class)
public class OWMHistoricalWeatherConfig {
}
