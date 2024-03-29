package com.nico.organizeHistoricalData;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrginizeHistoricalDataService {
    private final CityProperties cityProperties;
    private final String weatherCsvFormatter;
    private final Path rootPath;
    private final WEathergyRepository repository;

    @Autowired
    public OrginizeHistoricalDataService(CityProperties cityProperties, WEathergyRepository repository) {
        this.cityProperties = cityProperties;
        this.repository = repository;
        this.weatherCsvFormatter = "https://climate.weather.gc.ca/climate_data/bulk_data_e.html?format=csv&stationID=%d&Year=%d&Month=%d&Day=14&timeframe=1&submit=Data";
        this.rootPath = Paths.get(System.getProperty("user.dir"));
    }


    public String downloadWeatherCsv(String city, int year, int month){
        Map<String, Number> cityInfo = cityProperties.getCities().get(city);
        Number stationId = cityInfo.get("station");
        String url = String.format(weatherCsvFormatter, stationId, year, month);
        return url;
    }

    public List<WeatherCSVRow> createCityWeatherModel(String filepath) throws FileNotFoundException, IOException {
        try (FileReader fileReader = new FileReader(filepath)) {
            return new CsvToBeanBuilder<WeatherCSVRow>(fileReader)
                    .withType(WeatherCSVRow.class)
                    .build()
                    .parse();
        }
    }

    public void getWholeMonthData(int year, int month) throws IOException {
        Map<String, List<WeatherCSVRow>> citiesToWeather = new HashMap<>();

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Map<String, Number>> cities = cityProperties.getCities();
        // path to download csv files
        Path saveDir = rootPath.resolve("data/weather/" + year + '_' + month);
        Files.createDirectories(saveDir);       // make sure dir exist, will not throw exception  if dir already exist
        // go through all cities
        for (String city : cities.keySet()){
            Number stationId = cities.get(city).get("station");
            String url = String.format(weatherCsvFormatter, stationId, year, month);
            byte[] fileBytes = restTemplate.getForObject(url, byte[].class);
            if (fileBytes != null) {
                String fileName = String.format("%s_%d_%d_weather.csv", city, year, month);
                Path filePath = saveDir.resolve(fileName);
                Files.write(filePath, fileBytes);
                System.out.println("Downloaded " + fileName);
                List<WeatherCSVRow> cityWeatherModel = createCityWeatherModel(filePath.toString());
                citiesToWeather.put(city, cityWeatherModel);
            }
        }
        saveWEathergyData(citiesToWeather);
    }

    private void saveWEathergyData(Map<String, List<WeatherCSVRow>> citiesToWeather) {
        int count = citiesToWeather.get("Toronto").size();

        for (int i = 0; i < count; i++) {
            long dt = citiesToWeather.get("Toronto").get(i).getUnixTime();
            WEathergyData data = new WEathergyData(
                    dt,
                    citiesToWeather.get("Toronto").get(i).getTemp(),
                    citiesToWeather.get("Thunder_Bay").get(i).getTemp(),
                    citiesToWeather.get("Ottawa").get(i).getTemp(),
                    citiesToWeather.get("Timmins").get(i).getTemp()
            );
            repository.insert(data);
            System.out.println("Added new data: " + data.toString());
        }
    }
}
