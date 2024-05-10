package com.nico.webfluxbackend.dataClasses;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "WEathergyDB")
@Data
@AllArgsConstructor
public class WEathergyData {
    @Id
    private String id;
    @Indexed(unique = true)
    private long dt;
    private int demand;
    private double toronto_temp;
    private double thunder_bay_temp;
    private double ottawa_temp;
    private double timmins_temp;

    public WEathergyData() {
        this.dt = 0;
        this.demand = 0;
        this.toronto_temp = -1000.0;
        this.thunder_bay_temp = -1000.0;
        this.ottawa_temp = -1000.0;
        this.timmins_temp = -1000.0;
    }

    public WEathergyData(long dt) {
        this.dt = dt;
        this.demand = 0;
        this.toronto_temp = -1000.0;
        this.thunder_bay_temp = -1000.0;
        this.ottawa_temp = -1000.0;
        this.timmins_temp = -1000.0;
    }

    public WEathergyData(long dt, int demand) {
        this.dt = dt;
        this.demand = demand;
        this.toronto_temp = -1000.0;
        this.thunder_bay_temp = -1000.0;
        this.ottawa_temp = -1000.0;
        this.timmins_temp = -1000.0;
    }

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
        return String.format("%d : Toronto - %f°C, ThunderBay - %f°C, Ottawa - %f°C, Timmins - %f°C, demand = %d",
                this.dt,
                this.toronto_temp,
                this.thunder_bay_temp,
                this.ottawa_temp,
                this.timmins_temp,
                this.demand
                );
    }

    public void setTempByCity(String city, double temp) {
        switch (city) {
            case "Toronto" -> setToronto_temp(temp);
            case "Thunder_Bay" -> setThunder_bay_temp(temp);
            case "Ottawa" -> setOttawa_temp(temp);
            case "Timmins" -> setTimmins_temp(temp);
        }
    }

    public double getTempByCity(String city) {
        return switch (city) {
            case "Toronto" -> this.toronto_temp;
            case "Thunder_Bay" -> this.thunder_bay_temp;
            case "Ottawa" -> this.ottawa_temp;
            case "Timmins" -> this.timmins_temp;
            default -> -100;
        };
    }

    public boolean allNull() {
        return this.toronto_temp == -1000.0 &&
                this.thunder_bay_temp == -1000.0 &&
                this.ottawa_temp == -1000.0 &&
                this.timmins_temp == -1000.0;
    }
}
