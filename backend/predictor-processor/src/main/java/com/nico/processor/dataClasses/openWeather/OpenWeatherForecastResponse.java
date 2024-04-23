package com.nico.processor.dataClasses.openWeather;

import lombok.Data;

import java.util.List;

@Data
public class OpenWeatherForecastResponse {
    private double lat;
    private double lon;
    private String timezone;
    private List<OpenWeatherForecastResponseData> hourly;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(
                String.format("Hourly Forecast: Lat: %f; Lon: %f, Timezone: %s", lat, lon, timezone));
        for (OpenWeatherForecastResponseData dtData : hourly) {
            result.append("\n").append(dtData.toString());
        }
        return result.toString();
    }
}
