package com.nico.sink;

import com.mongodb.MongoWriteException;
import com.nico.sink.repository.DemandDataRepository;
import com.nico.sink.repository.WEathergyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SaveToDBService {
    private final DemandDataRepository demandDataRepository;
    private final WEathergyRepository wEathergyRepository;

    @Autowired
    public SaveToDBService(DemandDataRepository demandDataRepository, WEathergyRepository wEathergyRepository) {
        this.demandDataRepository = demandDataRepository;
        this.wEathergyRepository = wEathergyRepository;
    }

    public void saveToRepository(DemandData demandData){
        LocalDateTime currDT = demandData.getTimestamp();
        demandDataRepository.findDemandDataByTimestamp(currDT)
                // TODO: no need to print when already recorded.. how to dismiss it?
                // TODO: consider updating if value different?
                .ifPresentOrElse(
                        s -> System.out.println("Value at " + currDT + " already recorded"),
                        () -> {
                            demandDataRepository.insert(demandData);
                            System.out.println("Number of DemandData in MongoDB: " + demandDataRepository.count());
                        }
                );
    }

    public void saveWEathergyToDB(WEathergyData wEathergyData) {
        long dt = wEathergyData.getDt();
        wEathergyRepository.findWEathergyDataByDt(dt)
                .ifPresentOrElse(
                        existingWEathergy -> {
                            wEathergyData.setId(existingWEathergy.getId());
//                            existingWEathergy.setDemand(wEathergyData.getDemand());
//                            existingWEathergy.setToronto_temp(wEathergyData.getToronto_temp());
//                            existingWEathergy.setThunder_bay_temp(wEathergyData.getThunder_bay_temp());
//                            existingWEathergy.setOttawa_temp(wEathergyData.getOttawa_temp());
//                            existingWEathergy.setTimmins_temp(wEathergyData.getTimmins_temp());
                            wEathergyRepository.save(wEathergyData);
                            System.out.println("Updated WEathergyData: " + wEathergyData.toString());
                        },
                        () -> {
                            wEathergyRepository.insert(wEathergyData);
                            System.out.println("Inserted new WEathergyData: " + wEathergyData.toString());
                        }
                );
    }
}
