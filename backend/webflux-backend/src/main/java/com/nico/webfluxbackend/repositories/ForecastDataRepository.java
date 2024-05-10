package com.nico.webfluxbackend.repositories;

import com.nico.webfluxbackend.dataClasses.ForecastData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ForecastDataRepository extends ReactiveMongoRepository<ForecastData, String> {
}
