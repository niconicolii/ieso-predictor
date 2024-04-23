package com.nico.processor.dataClasses.openWeather;

import lombok.Data;

@Data
public class OpenWeatherForecastResponseData {
    private long dt;
    private double temp;

    @Override
    public String toString() {
        return String.format("  dt: %d; temp: %f", dt, temp);
    }
}
