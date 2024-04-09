//package com.nico.organizeHistoricalData.OpenWeatherMap;
//
//import com.nico.organizeHistoricalData.config.CityProperties;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//@Deprecated
////@Service
//public class OWMHistoricalWeatherService {
//    @Value("${openweathermap.api-key}")
//    private String apiKey;
//
//    @Value("${openweathermap.base-url}")
//    private String baseUrl;
//
//    private RestTemplate restTemplate = new RestTemplate();
//    private final CityProperties cityProperties;
//    private final OWMHistoricalWeatherRepository repository;
//    private final DateTimeFormatter formatter;
//
//    @Autowired
//    public OWMHistoricalWeatherService(CityProperties cityProperties,
//                                       OWMHistoricalWeatherRepository repository) {
//        this.cityProperties = cityProperties;
//        this.repository = repository;
//        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//    }
//
//    public String getFromApi(String cityName, long dt) {
//        List<Double> latlon = cityProperties.getCities().get(cityName);
//        String url = baseUrl + "/timemachine?lat=" + latlon.get(0) + "&lon=" + latlon.get(1) + "&dt=" + dt + "&appid=" + apiKey;
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//        return response.getBody();
//    }
//
//    public WeatherData createWeatherDataByApiCall(String cityName, long dt){
//        JSONObject jsonObj = new JSONObject(getFromApi(cityName, dt));
//        JSONArray dataArray = jsonObj.getJSONArray("data");
//        if (!dataArray.isEmpty()) {
//            JSONObject dataObj = dataArray.getJSONObject(0);
//            WeatherData weatherData = new WeatherData(cityName, dt, dataObj);
//            return weatherData;
//        }
//        return null;
//    }
//
//    public void insertWeatherData(WeatherData weatherData) {
//        repository.insert(weatherData);
//    }
//
//
//    public void updateId() {
//        List<WeatherData> allData = repository.findAll();
//        for (WeatherData data : allData) {
//            String oldID = data.getId();
//            if (!oldID.contains(" - ")){
//                String newID = oldID.replaceAll("([a-zA-Z_])(\\d)", "$1 - $2");
//                data.setId(newID);
//                repository.insert(data);
//                repository.deleteById(oldID);
//
//                System.out.println("Original ID: " + data.getId() + " New ID: " + newID);
//
//            }
//        }
//
//    }
//
//    public List<String> getRecordMissing() {
//        List<String> missingValue = new ArrayList<>();
//        List<CityWeather> cityWeathers = repository.groupWeatherByCity();
//        for (CityWeather cw : cityWeathers) {
//            long dt = cw.getWeatherList().get(0).getDt();
//            for (WeatherData w : cw.getWeatherList()) {
//                if (w.getDt() != dt) {
//                    String dtStr = LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneId.systemDefault())
//                            .format(formatter);
//                    missingValue.add(w.getCity()  + " missing weather info @ " + dtStr);
//                } else {
//                    dt += 3600L;
//                }
//            }
//        }
//        return missingValue;
//    }
//}
