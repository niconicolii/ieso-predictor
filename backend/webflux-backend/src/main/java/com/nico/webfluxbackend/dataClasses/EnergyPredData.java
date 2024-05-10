package com.nico.webfluxbackend.dataClasses;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "energyPrediction")
@Data
public class EnergyPredData {
    @Id
    private String id;
    @Indexed(unique = true)
    private LocalDateTime dt;
    private int value;

    public EnergyPredData(LocalDateTime dt, int value) {
        this.dt = dt;
        this.value = value;
    }
}
