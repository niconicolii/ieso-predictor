package com.nico.webfluxbackend.database;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WEathergyRepository extends ReactiveMongoRepository<WEathergyData, String> {
}
