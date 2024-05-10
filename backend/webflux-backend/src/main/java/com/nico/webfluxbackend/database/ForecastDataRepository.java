package com.nico.webfluxbackend.database;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ForecastDataRepository extends ReactiveMongoRepository<ForecastData, String> {
}
