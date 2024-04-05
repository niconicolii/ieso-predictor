package com.nico.source.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DemandDataRepository extends MongoRepository<DemandData, String> {
    Optional<DemandData> findTopByOrderByTimestampDesc();
}
