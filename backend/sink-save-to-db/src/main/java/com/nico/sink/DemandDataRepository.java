package com.nico.sink;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DemandDataRepository extends MongoRepository<DemandData, String> {
    Optional<DemandData> findDemandDataByTimestamp(LocalDateTime timestamp);
}
