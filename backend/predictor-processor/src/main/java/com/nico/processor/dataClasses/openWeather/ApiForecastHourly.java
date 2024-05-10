package com.nico.processor.dataClasses.openWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiForecastHourly {
    private long dt;
    private double temp;

    public ApiForecastHourly(long dt) {
        this.dt = dt;
        this.temp = -100.0;
    }

    @Override
    public String toString() {
        return String.format("  dt: %d; temp: %f", dt, temp);
    }
}
