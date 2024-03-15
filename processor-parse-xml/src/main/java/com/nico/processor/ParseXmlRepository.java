package com.nico.processor;

import com.nico.processor.dataClasses.DemandData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ParseXmlRepository extends MongoRepository<DemandData, String> {
    List<DemandData> findTop287ByOrderByTimestampDesc();
}
