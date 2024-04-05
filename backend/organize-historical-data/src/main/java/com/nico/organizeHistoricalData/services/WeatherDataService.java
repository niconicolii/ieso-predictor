package com.nico.organizeHistoricalData.services;

import com.nico.organizeHistoricalData.WeatherMappingStrategy;
import com.nico.organizeHistoricalData.config.CityProperties;
import com.nico.organizeHistoricalData.repository.WEathergyData;
import com.nico.organizeHistoricalData.repository.WEathergyRepository;
import com.nico.organizeHistoricalData.repository.WeatherCSVRow;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class WeatherDataService {
    private final Map<String, Map<String, Number>> cityProperties;
    private final String weatherCsvFormatter;
    private final Path weatherSaveDir;
    private final WEathergyService wEathergyService;

    @Autowired
    public WeatherDataService(CityProperties cityProperties, WEathergyService wEathergyService) {
        this.cityProperties = cityProperties.getCities();
        this.wEathergyService = wEathergyService;
        this.weatherCsvFormatter = "https://climate.weather.gc.ca/climate_data/bulk_data_e.html?format=csv&stationID=%d&Year=%d&Month=%d&Day=14&timeframe=1&submit=Data";
        Path rootPath = Paths.get(System.getProperty("user.dir"));
        this.weatherSaveDir = rootPath.resolve("data/weather");
    }

    public List<WeatherCSVRow> cvsToRowList(String filepath) throws FileNotFoundException, IOException {
        try (FileReader fileReader = new FileReader(filepath)) {
            return new CsvToBeanBuilder<WeatherCSVRow>(fileReader)
                    .withType(WeatherCSVRow.class)
                    .withMappingStrategy(new WeatherMappingStrategy<>(WeatherCSVRow.class))
                    .build()
                    .parse();
        }
    }

    public String downloadCityCsv(int year, int month, String city, int stationId) throws IOException {
        List<WeatherCSVRow> singleCityCsvRowList = new ArrayList<>();

        if (stationId == 0) {
            stationId = (int) cityProperties.get(city).get("station");
        }
        String url = String.format(weatherCsvFormatter, stationId, year, month);
        RestTemplate restTemplate = new RestTemplate();
        byte[] fileBytes = restTemplate.getForObject(url, byte[].class);
        if (fileBytes != null) {
            Path saveDir = weatherSaveDir.resolve(year + "_" + month);
            Files.createDirectories(saveDir);

            String fileName = String.format("%s_%d_%d_weather.csv", city, year, month);
            Path filePath = saveDir.resolve(fileName);
            Files.write(filePath, fileBytes);
            System.out.println("Downloaded " + filePath);
            return filePath.toString();
        }
        throw new IOException(
                String.format(
                        "Failed to download %d-%d weather csv file for %s.",
                        year, month, city)
        );
    }

    // Download all csv files for different cities containing weather information of given year and month
    // Returns a map that maps city name to list of WeatherCSVRow which each represents an hour in the month and
    // its temperature of that hour
    private Map<String, List<WeatherCSVRow>> getCitiesToWeatherMapping(int year, int month) throws IOException {
        Map<String, List<WeatherCSVRow>> citiesToWeather = new HashMap<>();
        // path to download csv files

        Files.createDirectories(weatherSaveDir);       // make sure dir exist, will not throw exception  if dir already exist
        // go through all cities
        for (String city : cityProperties.keySet()){
            String filePath = downloadCityCsv(year, month, city, 0);
            List<WeatherCSVRow> cityWeatherModel = cvsToRowList(filePath);
            citiesToWeather.put(city, cityWeatherModel);
        }
        return citiesToWeather;
    }

    public void getWholeMonthData(int year, int month) throws IOException {
        Map<String, List<WeatherCSVRow>> citiesToWeather = getCitiesToWeatherMapping(year, month);
        wEathergyService.saveWEathergyData(citiesToWeather);
    }

    public void getWholeYearData(int year) throws IOException {
        int maxMonth = 12;
        LocalDate today = LocalDate.now();
        if (today.getYear() <= year) {
            maxMonth = today.getMonthValue();
        }
        for (int i = 1; i <= maxMonth; i++) {
            getWholeMonthData(year, i);
        }
    }

    public List<Integer> getWEathergyUpToDate() throws IOException {
        List<Integer> updatedYears = new ArrayList<>();

        LocalDate today = LocalDate.now();
        final int maxYear = today.getYear();
        final int maxMonth = today.getMonthValue();
        int minYear = 2022;
        int minMonth = 1;

        Optional<WEathergyData> optional =  wEathergyService.getMaxDtData();
        if (optional.isPresent()) {
            long dt = optional.get().getDt();
            ZoneId zoneId = ZoneId.of("America/New_York");
            Instant instant = Instant.ofEpochSecond(dt);
            LocalDateTime dbMaxDT = instant.atZone(zoneId).toLocalDateTime();
            minYear = dbMaxDT.getYear();
            minMonth = dbMaxDT.getMonthValue();
        }
        for (int year = minYear; year <= maxYear; year++) {
            for (int month = minMonth; month <= maxMonth; month++) {
                getWholeMonthData(year, month);
            }
            updatedYears.add(year);
        }
        return updatedYears;
    }

    public void getStationSpecificWeatherData(int year, int month, String city, int station) throws IOException {
        String filePath = downloadCityCsv(year, month, city + "-" + station, station);
        List<WeatherCSVRow> weatherCSVRows = cvsToRowList(filePath);
        wEathergyService.saveWEathergyDataForSingle(weatherCSVRows, city);
    }
}
