package com.nico.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SaveToDBService {
    private final DemandDataRepository repository;

    @Autowired
    public SaveToDBService(DemandDataRepository repository) {
        this.repository = repository;
    }

    public void saveToRepository(DemandData demandData){
        LocalDateTime currDT = demandData.getTimestamp();
        repository.findDemandDataByTimestamp(currDT)
                // TODO: no need to print when already recorded.. how to dismiss it?
                // TODO: consider updating if value different?
                .ifPresentOrElse(
                        s -> System.out.println("Value at " + currDT + " already recorded"),
                        () -> {
                            repository.insert(demandData);
                            System.out.println("Number of DemandData in MongoDB: " + repository.count());
                        }
                );
    }

}
