package com.nico.openweathermaphistoricalweathergetter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.nico.openweathermaphistoricalweathergetter.CityProperties")
public class OpenWeatherMapHistoricalWeatherGetterApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenWeatherMapHistoricalWeatherGetterApplication.class, args);
	}

}
