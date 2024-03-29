package com.nico.organizeHistoricalData;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WEathergyRepository extends MongoRepository<WEathergyData, String> {

}
