package com.nico.webfluxbackend.repositories;

import com.nico.webfluxbackend.dataClasses.EnergyPredData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface EnergyPredRepository extends ReactiveMongoRepository<EnergyPredData, String> {
}
