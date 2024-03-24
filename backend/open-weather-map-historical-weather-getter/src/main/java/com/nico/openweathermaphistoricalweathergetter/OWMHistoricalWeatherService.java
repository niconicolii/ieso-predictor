package com.nico.openweathermaphistoricalweathergetter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OWMHistoricalWeatherService {
    @Value("${openweathermap.api-key}")
    private String apiKey;

    @Value("${openweathermap.base-url}")
    private String baseUrl;

    private RestTemplate restTemplate = new RestTemplate();
    private final CityProperties cityProperties;
    private final OWMHistoricalWeatherRepository repository;

    @Autowired
    public OWMHistoricalWeatherService(CityProperties cityProperties,
                                       OWMHistoricalWeatherRepository repository) {
        this.cityProperties = cityProperties;
        this.repository = repository;
    }

    public String getFromApi(String cityName, long dt) {
        List<Double> latlon = cityProperties.getCities().get(cityName);
        String url = baseUrl + "/timemachine?lat=" + latlon.get(0) + "&lon=" + latlon.get(1) + "&dt=" + dt + "&appid=" + apiKey;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    public WeatherData createWeatherDataByApiCall(String cityName, long dt){
        JSONObject jsonObj = new JSONObject(getFromApi(cityName, dt));
        JSONArray dataArray = jsonObj.getJSONArray("data");
        if (!dataArray.isEmpty()) {
            JSONObject dataObj = dataArray.getJSONObject(0);
            WeatherData weatherData = new WeatherData(cityName, dt, dataObj);
            return weatherData;
        }
        return null;
    }

    public void insertWeatherData(WeatherData weatherData) {
        repository.insert(weatherData);
    }
}
