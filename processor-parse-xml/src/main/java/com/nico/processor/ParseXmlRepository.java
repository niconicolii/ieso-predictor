package com.nico.processor;

import com.nico.processor.dataClasses.DemandData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ParseXmlRepository extends MongoRepository<DemandData, String> {
    Optional<DemandData> findTopByOrderByTimestampDesc();
}
