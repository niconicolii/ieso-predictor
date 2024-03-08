package com.nico.sink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

@Service
public class SaveToDBService {

    private final ObjectMapper objectMapper;

    public SaveToDBService() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public DemandData deserializeDemandData(String serializedStr) throws JsonProcessingException {
        return objectMapper.readValue(serializedStr, DemandData.class);
    }
}
