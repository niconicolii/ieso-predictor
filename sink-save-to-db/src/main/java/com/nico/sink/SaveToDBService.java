package com.nico.sink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaveToDBService {
    private final DemandDataRepository repository;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public SaveToDBService(DemandDataRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public DemandData deserializeDemandData(String serializedStr) throws JsonProcessingException {
        return objectMapper.readValue(serializedStr, DemandData.class);
    }

    public void saveToRepository(DemandData demandData){
        // use Mongo Core to find entity with timestamp
//        findUsingMongoCore(demandData);
        LocalDateTime currDT = demandData.getTimestamp();
        repository.findDemandDataByTimestamp(currDT)
                // TODO: no need to pring when already recorded.. how to dismiss it?
                .ifPresentOrElse(
                        s -> System.out.println("Value at " + currDT + " already recorded"),
                        () -> {
                            repository.insert(demandData);
                            System.out.println("Number of DemandData in MongoDB: " + repository.count());
                        }
                );
    }

}
