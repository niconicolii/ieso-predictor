package com.nico.organizeHistoricalData.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WEathergyRepository extends MongoRepository<WEathergyData, String> {

    Optional<WEathergyData> findWEathergyDataByDt(long dt);

    Optional<WEathergyData> findTopByOrderByDtDesc();
}
