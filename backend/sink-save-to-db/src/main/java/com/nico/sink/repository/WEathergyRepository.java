package com.nico.sink.repository;

import com.nico.sink.dataClasses.WEathergyData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WEathergyRepository extends MongoRepository<WEathergyData, String> {
    Optional<WEathergyData> findWEathergyDataByDt(long dt);
}
