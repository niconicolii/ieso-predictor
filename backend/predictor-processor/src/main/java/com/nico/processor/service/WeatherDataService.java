package com.nico.processor.service;

import com.nico.processor.csv.WeatherMappingStrategy;
import com.nico.processor.dataClasses.ForecastData;
import com.nico.processor.dataClasses.WEathergyData;
import com.nico.processor.csv.WeatherCSVRow;
import com.nico.processor.dataClasses.WEathergyMissingMessage;
import com.nico.processor.dataClasses.openWeather.ApiForecastResp;
import com.nico.processor.dataClasses.openWeather.ApiForecastHourly;
import com.nico.processor.dataClasses.openWeather.ApiWeatherAtTimestampResp;
import com.nico.processor.dataClasses.openWeather.ApiWeatherAtTimestampData;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class WeatherDataService {
    @Value("${openWeather.apiKey}")
    private String openWeatherApiKey;
    private final Path weatherSaveDir;
    private final ZoneId zoneId = ZoneId.of("America/New_York");
    private static final Logger LOGGER = Logger.getLogger(WeatherDataService.class.getName());
    private final RestTemplate restTemplate = new RestTemplate();


    @Autowired
    public WeatherDataService() throws IOException {
        Path rootPath = Paths.get(System.getProperty("user.dir"));
        this.weatherSaveDir = rootPath.resolve("data/weather");
        Files.createDirectories(this.weatherSaveDir);       // make sure dir exist, will not throw exception  if dir already exist
    }

    private long getCurrHourTimestamp() {
        return ZonedDateTime.now(zoneId).withMinute(0).withSecond(0).toEpochSecond();
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

    public String downloadCityCsv(String url, int year, int month) throws IOException {
        byte[] fileBytes = restTemplate.getForObject(url, byte[].class);
        String city = url.split("stationID=")[1].split("&")[0];
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

    private Map<String, List<WeatherCSVRow>> getCitiesToWeatherCsvRows(
            Map<String, String> cityToWeatherCsvUrl, int year, int month) throws IOException {
        Map<String, List<WeatherCSVRow>> mapCitiesToWeatherRows = new HashMap<>();

        // go through all cities
        for (String city : cityToWeatherCsvUrl.keySet()){
            String filePath = downloadCityCsv(cityToWeatherCsvUrl.get(city), year, month);
            List<WeatherCSVRow> cityWeatherModel = cvsToRowList(filePath);
            mapCitiesToWeatherRows.put(city, cityWeatherModel);
        }
        return mapCitiesToWeatherRows;
    }

    private long ymdhToEpoch(int year, int month, int day, int hour) {
        return ZonedDateTime.of(year, month, day, hour, 0, 0,0, zoneId).toEpochSecond();
    }

    private double getTempFromApiCall(String url, long dt) {
        try {
            ApiWeatherAtTimestampResp response = restTemplate.getForObject(url, ApiWeatherAtTimestampResp.class);
            if (response == null) {
                throw new IOException("Empty response from OpenWeatherAPI when retrieving historical data, url: " + url);
            }
            ApiWeatherAtTimestampData data = response.getData().get(0);
            if (data.getDt() != dt) {
                throw new IOException("Got wrong weather info from OpenWeather API!\n" +
                        "[Expected DT] " + dt +
                        "[Response] " + data.toString());
            }
            return data.getTemp();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting temp from OpenWeather API for dt: " + dt +
                    " ! Using dummy temperature. ErrorMsg: " + e.getMessage());
            return -100.0;
        }
    }

    private LinkedList<ApiForecastHourly> getForecastFromApiCall(String url) {
        LinkedList<ApiForecastHourly> result = new LinkedList<>();
        try {
            ApiForecastResp response = restTemplate.getForObject(url, ApiForecastResp.class);
            if (response == null) {
                throw new IOException("Empty response from OpenWeatherAPI.");
            }
            result = new LinkedList<>(response.getHourly());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving forecast from API, returning dummy list. ErrorMsg: " +
                    e.getMessage() + "; url: " + url);
            LOGGER.log(Level.SEVERE, "If just getting as string:" + restTemplate.getForObject(url, String.class));
            long dt = getCurrHourTimestamp();
            for (int i = 0; i < 48; i++) {
                result.add(new ApiForecastHourly(dt));
                dt += 3600L;
            }
        }
        return result;
    }

    private List<WEathergyData> createWEathergyByDt(WEathergyMissingMessage msg,
                                                    int year,
                                                    int month,
                                                    Map<String, List<WeatherCSVRow>> citiesToWeatherRow) {
        List<WEathergyData> wEathergyDataList = new ArrayList<>();

        long startOfMonth = ymdhToEpoch(year, month, 1, 0);
        for (long dt : msg.getDts()) {
            WEathergyData data = new WEathergyData(dt);
            int index = (int) ((dt - startOfMonth) / 3600);
            for (String city : citiesToWeatherRow.keySet()) {
                double temperature = citiesToWeatherRow.get(city).get(index).getTemp();
                if (temperature <= -100.0) {
                    // couldn't get value from csv, then we will have to get from api call.
                    String url = msg.getCityToWeatherApiUrlPrefix().get(city) + dt + "&appid=" + openWeatherApiKey;
                    temperature = getTempFromApiCall(url, dt);
                }
                data.setTempByCity(city, temperature);
            }
            wEathergyDataList.add(data);
        }
        return wEathergyDataList;
    }

    public List<WEathergyData> createMissingWEathergyData(WEathergyMissingMessage msgInfo) throws IOException {
        long dt = msgInfo.getDts().get(0);
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), zoneId);
        int year = ldt.getYear();
        int month = ldt.getMonthValue();

        Map<String, List<WeatherCSVRow>> citiesToWeatherCsvRows = getCitiesToWeatherCsvRows(
                msgInfo.getCityToWeatherCsvUrl(), year, month
        );
        return createWEathergyByDt(msgInfo, year, month, citiesToWeatherCsvRows);
    }

    public List<ForecastData> getForecasts(Map<String, String> cityUrls) {
        List<ForecastData> forecastDataList = new ArrayList<>();
        Map<String, LinkedList<ApiForecastHourly>> cityToForecasts = new HashMap<>();
        List<String> cities = cityUrls.keySet().stream().toList();

        for (String city : cities) {
            cityToForecasts.put(city, getForecastFromApiCall(cityUrls.get(city) + openWeatherApiKey));
        }
        long timestamp = getCurrHourTimestamp() + 3600L;
        for (int i = 0; i < 47; i++) {
            ForecastData forecastData = new ForecastData(timestamp);
            for (String city: cities) {
                ApiForecastHourly firstForecast = cityToForecasts.get(city).peek();
                while (firstForecast != null && firstForecast.getDt() < timestamp) {
                    cityToForecasts.get(city).removeFirst();
                    firstForecast = cityToForecasts.get(city).peek();
                }
                if (firstForecast != null && firstForecast.getDt() == timestamp) {
                    forecastData.setTempByCity(city, firstForecast.getTemp());
                    cityToForecasts.get(city).removeFirst();
//                    LOGGER.log(Level.INFO, "Added temperature to forecastData for " + city + " at " + timestamp);
                } else {
                    forecastData.setTempByCity(city, -100.0);
                    LOGGER.log(Level.SEVERE, "No info for " + city + " at " + timestamp + ", using -100.0");
                }
            }
            forecastDataList.add(forecastData);
            timestamp += 3600L;
        }
        return forecastDataList;
    }
}
