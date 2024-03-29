package com.nico.organizeHistoricalData;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "WEathergyModelDatabase")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WEathergyData {
    @Id
    private String id;
    @Indexed(unique = true)
    private long dt;
    private double demand;
    private double toronto_temp;
    private double thunder_bay_temp;
    private double ottawa_temp;
    private double timmins_temp;

    public WEathergyData(long dt, double toronto_temp, double thunder_bay_temp, double ottawa_temp, double timmins_temp) {
        this.dt = dt;
        this.toronto_temp = toronto_temp;
        this.thunder_bay_temp = thunder_bay_temp;
        this.ottawa_temp = ottawa_temp;
        this.timmins_temp = timmins_temp;
        this.demand = -1;
    }

    @Override
    public String toString() {
        return String.format("%d : Toronto - %f°C, ThunderBay - %f°C, Ottawa - %f°C, Timmins - %f°C, demand = %f",
                this.dt,
                this.toronto_temp,
                this.thunder_bay_temp,
                this.ottawa_temp,
                this.timmins_temp,
                this.demand
                );
    }
}
