package com.nico.sink.repository;

import com.nico.sink.dataClasses.ForecastData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastRepository extends MongoRepository<ForecastData, String> {
}
