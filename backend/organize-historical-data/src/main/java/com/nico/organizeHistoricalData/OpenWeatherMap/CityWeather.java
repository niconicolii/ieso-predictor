package com.nico.organizeHistoricalData.OpenWeatherMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityWeather {
    private String city;
    private int count;
    private List<WeatherData> weatherList;
}
