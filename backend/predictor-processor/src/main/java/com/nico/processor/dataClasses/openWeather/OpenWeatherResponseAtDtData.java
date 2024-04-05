package com.nico.processor.dataClasses.openWeather;

import lombok.Data;

@Data
public class OpenWeatherResponseAtDtData {
    private long dt;
    private long sunrise;
    private long sunset;
    private double temp;
    private double uvi;

    @Override
    public String toString() {
        return String.format("  dt: %d; temp: %f", dt, temp);
    }
}
