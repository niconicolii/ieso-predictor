package com.nico.processor.dataClasses.openWeather;

import lombok.Data;

import java.util.List;

@Data
public class OpenWeatherResponseAtDt {
    private double lat;
    private double lon;
    private String timezone;
    private List<OpenWeatherResponseAtDtData> data;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.format("Lat: %d; Lon: %d, Timezone: %s", lat, lon, timezone));
        for (OpenWeatherResponseAtDtData dtData : data) {
            result.append("\n").append(dtData.toString());
        }
        return result.toString();
    }
}
