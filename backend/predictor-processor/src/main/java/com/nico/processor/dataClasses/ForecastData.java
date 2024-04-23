package com.nico.processor.dataClasses;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
public class ForecastData {
    @Id
    private String id;
    @Indexed(unique = true)
    private long dt;
    private double toronto_temp;
    private double thunder_bay_temp;
    private double ottawa_temp;
    private double timmins_temp;

    public ForecastData(long dt) {
        this.dt = dt;
    }

    public void setTempByCity(String city, double temp) {
        switch (city) {
            case "Toronto" -> setToronto_temp(temp);
            case "Thunder_Bay" -> setThunder_bay_temp(temp);
            case "Ottawa" -> setOttawa_temp(temp);
            case "Timmins" -> setTimmins_temp(temp);
        }
    }
}
