package com.nico.sink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.MongoSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@Service
public class SaveToDBService {
    private final DemandDataRepository repository;
    private final ObjectMapper objectMapper;

    @Autowired
    public SaveToDBService(DemandDataRepository repository) {
        this.repository = repository;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public DemandData deserializeDemandData(String serializedStr) throws JsonProcessingException {
        return objectMapper.readValue(serializedStr, DemandData.class);
    }

    public void saveToRepository(DemandData demandData){

//        SaveDemandData newDemandData = new SaveDemandData(demandData.getTimestamp(), demandData.getValue());
        repository.insert(demandData);
        System.out.println("???????? count: " + repository.count());
    }
}
