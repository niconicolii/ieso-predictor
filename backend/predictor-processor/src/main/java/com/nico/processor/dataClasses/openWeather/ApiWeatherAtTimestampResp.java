package com.nico.processor.dataClasses.openWeather;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ApiWeatherAtTimestampResp {
    private double lat;
    private double lon;
    private String timezone;
    private List<ApiWeatherAtTimestampData> data;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.format("Lat: %f; Lon: %f, Timezone: %s", lat, lon, timezone));
        for (ApiWeatherAtTimestampData dtData : data) {
            result.append("\n").append(dtData.toString());
        }
        return result.toString();
    }
}
