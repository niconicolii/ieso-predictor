package com.nico.organizeHistoricalData.services;

import com.nico.organizeHistoricalData.repository.WEathergyData;
import com.nico.organizeHistoricalData.repository.WEathergyRepository;
import com.nico.organizeHistoricalData.repository.WeatherCSVRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class WEathergyService {

    private final WEathergyRepository repository;

    @Autowired
    public WEathergyService(WEathergyRepository repository) {
        this.repository = repository;
    }

    public void saveWEathergyData(Map<String, List<WeatherCSVRow>> citiesToWeather) {
        long currUnix = Instant.now().getEpochSecond();

        int count = citiesToWeather.get("Toronto").size();
        for (int i = 0; i < count; i++) {
            long dt = citiesToWeather.get("Toronto").get(i).getUnixTime();
            if (dt >= currUnix) break;

            int currIndex = i;
            // need to check if entity already exist in repository
            repository.findWEathergyDataByDt(dt).ifPresentOrElse(
                    // if found entity with dt in repository, update
                    wEathergyData -> {
                        updateTemp(wEathergyData, citiesToWeather, currIndex);
                    },
                    // otherwise create a new entity and insert
                    () -> {
                        WEathergyData data = new WEathergyData(dt,
                                citiesToWeather.get("Toronto").get(currIndex).getTemp(),
                                citiesToWeather.get("Thunder_Bay").get(currIndex).getTemp(),
                                citiesToWeather.get("Ottawa").get(currIndex).getTemp(),
                                citiesToWeather.get("Timmins").get(currIndex).getTemp()
                        );
                        if (!data.allNull()) {
                            repository.insert(data);
                            System.out.println("Added new weather data: " + data.toString());
                        }
                    }
            );
        }
    }

    public void saveWEathergyDataForSingle(List<WeatherCSVRow> weatherCSVRows, String city) {
        long currUnix = Instant.now().getEpochSecond();
        for (WeatherCSVRow row : weatherCSVRows) {
            long dt = row.getUnixTime();
            if (dt >= currUnix) break;
            repository.findWEathergyDataByDt(dt).ifPresent(
                    wEathergyData -> {
                        if (wEathergyData.getTempByCity(city) < -100.0) {
                            wEathergyData.setTempByCity(city, row.getTemp());
                            repository.save(wEathergyData);
                            System.out.println("Updated WEathergy with special station in " +
                                    city + ": " + wEathergyData.toString());
                        }
                    }
            );
        }
    }

    public void updateTemp(WEathergyData wEathergyData, Map<String, List<WeatherCSVRow>> citiesToWeather, int i) {
        boolean needUpdate = false;
        Set<String> cityNames = citiesToWeather.keySet();
        for (String city : cityNames) {
            double newTemp = citiesToWeather.get(city).get(i).getTemp();
            if (wEathergyData.getTempByCity(city) != newTemp && newTemp > -100.0) {
                wEathergyData.setTempByCity(city, newTemp);
                needUpdate = true;
            }
        }
        if (needUpdate) {
            repository.save(wEathergyData);
            System.out.println("Updated existing data: " + wEathergyData.toString());
        } else {
//            System.out.println("No update required with exact same data.");
        }
    }

    public Optional<WEathergyData> getMaxDtData() {
        return repository.findTopByOrderByDtDesc();
    }

    public Optional<WEathergyData> getWEathergyByDt(long dt) {
        return repository.findWEathergyDataByDt(dt);
    }
}
