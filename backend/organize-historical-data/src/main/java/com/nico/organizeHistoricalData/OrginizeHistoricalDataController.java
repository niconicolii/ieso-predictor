package com.nico.organizeHistoricalData;

import com.nico.organizeHistoricalData.repository.WeatherCSVRow;
import com.nico.organizeHistoricalData.services.EnergyDataService;
import com.nico.organizeHistoricalData.services.WeatherDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class OrginizeHistoricalDataController {
    private final WeatherDataService weatherService;
    private final EnergyDataService energyService;

    @Autowired
    public OrginizeHistoricalDataController(WeatherDataService weatherService, EnergyDataService energyService) {
        this.weatherService = weatherService;
        this.energyService = energyService;
    }

    @GetMapping("/city_weather_csv")
    public List<WeatherCSVRow> downloadWeatherCsv(
            @RequestParam("city") String city,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) throws IOException {
        String filePath = weatherService.downloadCityCsv(year, month, city, 0);
        return weatherService.cvsToRowList(filePath);

    }

    @GetMapping("/month_weather_csv")
    public String downloadWeatherCsv(
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) throws IOException {
        weatherService.getWholeMonthData(year, month);
        return "Updated WEathergyDB with year " + year + " month " + month + " weather data!";
    }

    @GetMapping("/year_weather_csv")
    public String downloadYearWeatherCsv(@RequestParam("year") int year) throws IOException {
        weatherService.getWholeYearData(year);
        return "Updated WEathergyDB with year " + year + " weather data!";
    }

    @GetMapping("/energy_data")
    public String updateEnergyData(@RequestParam("year") int year) throws IOException {
        energyService.addEnergyDemandToDB(year);
        return "Updated energy demand for year " + year;
    }

    @GetMapping("/year_all_weathergy")
    public String updateWeatherAndEnergyData(@RequestParam("year") int year) throws IOException {
        weatherService.getWholeYearData(year);
        energyService.addEnergyDemandToDB(year);
        return "Updated Full WEathergy Data for year " + year;
    }

    @GetMapping("/update_weathergy")
    public String updateLatestWeatherAndEnergyData() throws IOException {
        List<Integer> updatedYears = weatherService.getWEathergyUpToDate();
        for (int year : updatedYears) {
            energyService.addEnergyDemandToDB(year);
        }
        if (!updatedYears.isEmpty()) {
            return "Updated Full WEathergy Data for year(s): " + updatedYears.toString();
        }
        return "Already up to date.";
    }

    @GetMapping("/specific_station_on_month")
    public String fillMissingValueWithSpecificStation(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam("city") String city,
            @RequestParam("station") int station
    ) throws IOException {
        weatherService.getStationSpecificWeatherData(year, month, city, station);
        return "Updated missing values for " + city + " in " + year + "-" + month + " with station " + station + " values.";
    }


}
