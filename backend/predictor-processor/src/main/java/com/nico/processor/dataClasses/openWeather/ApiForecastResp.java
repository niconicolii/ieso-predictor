package com.nico.processor.dataClasses.openWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ApiForecastResp {
    private double lat;
    private double lon;
    private String timezone;
    private List<ApiForecastHourly> hourly;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(
                String.format("Hourly Forecast: Lat: %f; Lon: %f, Timezone: %s", lat, lon, timezone));
        for (ApiForecastHourly dtData : hourly) {
            result.append("\n").append(dtData.toString());
        }
        return result.toString();
    }
}
