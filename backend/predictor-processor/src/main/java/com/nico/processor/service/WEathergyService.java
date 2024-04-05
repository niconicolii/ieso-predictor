package com.nico.processor.service;

import com.nico.processor.dataClasses.WEathergyData;
import com.nico.processor.csv.WeatherCSVRow;
import com.nico.processor.dataClasses.WEathergyMissingMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class WEathergyService {

    private final WeatherDataService weatherDataService;
    private final EnergyDataService energyDataService;
    private final ZoneId zoneId = ZoneId.of("America/New_York");

    @Autowired
    public WEathergyService(WeatherDataService weatherDataService, EnergyDataService energyDataService) {
        this.weatherDataService = weatherDataService;
        this.energyDataService = energyDataService;
    }


    public List<WEathergyData> createMissingWEathergyData(WEathergyMissingMessage msgInfo) throws IOException {
        long dt = msgInfo.getDts().get(0);
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), zoneId);
        int year = ldt.getYear();
        int month = ldt.getMonthValue();

        Map<String, List<WeatherCSVRow>> citiesToWeatherCsvRows = weatherDataService.getCitiesToWeatherCsvRows(
                msgInfo.getCityToWeatherCsvUrl(), year, month
        );
        List<WEathergyData> wEathergyDataList = weatherDataService.createWEathergyByDt(
                msgInfo, year, month, citiesToWeatherCsvRows);
        return wEathergyDataList;
    }
}
