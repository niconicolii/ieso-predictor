package com.nico.organizeHistoricalData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class OrginizeHistoricalDataController {
    private final OrginizeHistoricalDataService service;

    @Autowired
    public OrginizeHistoricalDataController(OrginizeHistoricalDataService service) {
        this.service = service;
    }

    @GetMapping("/city_weather_csv")
    public String downloadWeatherCsv(
            @RequestParam("city") String city,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        return service.downloadWeatherCsv(city, year, month);
    }

    @GetMapping("/weather_csv")
    public String downloadWeatherCsv(
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) throws IOException {
        service.getWholeMonthData(year, month);
        return "Finished downloading?";
    }
}
