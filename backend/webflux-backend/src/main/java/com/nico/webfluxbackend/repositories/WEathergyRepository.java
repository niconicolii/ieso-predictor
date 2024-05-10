package com.nico.webfluxbackend.repositories;

import com.nico.webfluxbackend.dataClasses.WEathergyData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WEathergyRepository extends ReactiveMongoRepository<WEathergyData, String> {
}
