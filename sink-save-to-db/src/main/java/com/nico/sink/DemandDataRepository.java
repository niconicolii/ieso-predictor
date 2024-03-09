package com.nico.sink;

import org.springframework.data.mongodb.repository.MongoRepository;

//public interface DemandDataRepository extends MongoRepository<SaveDemandData, String> {
public interface DemandDataRepository extends MongoRepository<DemandData, String> {
}
