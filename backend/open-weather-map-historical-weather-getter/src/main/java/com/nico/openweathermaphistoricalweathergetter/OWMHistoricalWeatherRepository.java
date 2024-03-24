package com.nico.openweathermaphistoricalweathergetter;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OWMHistoricalWeatherRepository extends MongoRepository<WeatherData, String> {
}
