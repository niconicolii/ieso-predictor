package com.nico.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nico.processor.csv.WeatherMappingStrategy;
import com.nico.processor.dataClasses.ForecastData;
import com.nico.processor.dataClasses.WEathergyData;
import com.nico.processor.csv.WeatherCSVRow;
import com.nico.processor.dataClasses.WEathergyMissingMessage;
import com.nico.processor.dataClasses.openWeather.OpenWeatherForecastResponse;
import com.nico.processor.dataClasses.openWeather.OpenWeatherForecastResponseData;
import com.nico.processor.dataClasses.openWeather.OpenWeatherResponseAtDt;
import com.nico.processor.dataClasses.openWeather.OpenWeatherResponseAtDtData;
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
    private final String weatherCsvFormatter;
    private final Path weatherSaveDir;
    private final ZoneId zoneId = ZoneId.of("America/New_York");
    private static final Logger LOGGER = Logger.getLogger(WeatherDataService.class.getName());


    @Autowired
    public WeatherDataService() throws IOException {
        this.weatherCsvFormatter = "https://climate.weather.gc.ca/climate_data/bulk_data_e.html?format=csv&stationID=%d&Year=%d&Month=%d&Day=14&timeframe=1&submit=Data";
        Path rootPath = Paths.get(System.getProperty("user.dir"));
        this.weatherSaveDir = rootPath.resolve("data/weather");
        Files.createDirectories(this.weatherSaveDir);       // make sure dir exist, will not throw exception  if dir already exist
    }

    private long getCurrHourTimestamp() {
        return ZonedDateTime.now(zoneId).withMinute(0).withSecond(0).toEpochSecond();
    }

    private boolean validResponseFromOpenWeatherApi(String response) {
        return false;
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
        List<WeatherCSVRow> singleCityCsvRowList = new ArrayList<>();

        RestTemplate restTemplate = new RestTemplate();
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

    private long localToUnix(int year, int month, int day, int hour) {
        return LocalDateTime.of(year, month, day, hour, 0).atZone(zoneId).toEpochSecond();
    }

    private double getTempFromApiCall(String url, long dt) {
        System.out.println("Calling API from [getTempFromApiCall]");
        RestTemplate restTemplate = new RestTemplate();
        try {
            OpenWeatherResponseAtDt response = restTemplate.getForObject(url, OpenWeatherResponseAtDt.class);
            if (response == null) {
                throw new IOException("Empty response from OpenWeatherAPI when retrieving historical data, url: " + url);
            }
            OpenWeatherResponseAtDtData data = response.getData().get(0);
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

    private LinkedList<OpenWeatherForecastResponseData> getForecastFromApiCall(String url) {
        System.out.println("Calling API from [getForecastFromApiCall]");
        LinkedList<OpenWeatherForecastResponseData> result = new LinkedList<>();
        RestTemplate restTemplate = new RestTemplate();
        try {
            OpenWeatherForecastResponse response = restTemplate.getForObject(url, OpenWeatherForecastResponse.class);
//            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                throw new IOException("Empty response from OpenWeatherAPI.");
            }
//            ObjectMapper mapper = new ObjectMapper();
//            OpenWeatherForecastResponse responseClass = mapper.readValue(response, OpenWeatherForecastResponse.class);
//            System.out.println(responseClass.toString());
            result = new LinkedList<>(response.getHourly());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving forecast from API, returning dummy list. ErrorMsg: " +
                    e.getMessage() + "; url: " + url);
            LOGGER.log(Level.SEVERE, "If just getting as string:" + restTemplate.getForObject(url, String.class));
            long dt = getCurrHourTimestamp();
            for (int i = 0; i < 48; i++) {
                result.add(new OpenWeatherForecastResponseData(dt));
                dt += 3600L;
            }
        }
        return result;
    }

    private List<WEathergyData> createWEathergyByDt(WEathergyMissingMessage msg,
                                                    int year,
                                                    int month,
                                                    Map<String, List<WeatherCSVRow>> citiesToWeatherRow) throws IOException {
        List<WEathergyData> wEathergyDataList = new ArrayList<>();

        long startOfMonth = localToUnix(year, month, 1, 0);
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

    public List<ForecastData> getForecasts(Map<String, String> cityUrls) throws IOException {
        long currHrTimestamp = getCurrHourTimestamp();
        List<ForecastData> forecastDataList = new ArrayList<>();
        Map<String, LinkedList<OpenWeatherForecastResponseData>> cityToForecasts = new HashMap<>();
        List<String> cities = cityUrls.keySet().stream().toList();

        for (String city : cities) {
            cityToForecasts.put(city, getForecastFromApiCall(cityUrls.get(city) + openWeatherApiKey));
        }
//        for (int i = 0; i < 48; i++) {
//            long timestamp = cityToForecasts.get(cities.get(0)).get(i).getDt();
//            ForecastData forecastData = new ForecastData(timestamp);
//            for (String city : cities) {
//                OpenWeatherForecastResponseData cityCurrForecast = cityToForecasts.get(city).get(i);
//                double temperature;
//                if (cityCurrForecast.getDt() != timestamp) {
//                    LOGGER.log(Level.SEVERE, "Wrong timestamp for forecast!! Expected timestamp=" + timestamp +
//                            " Got timestamp=" + cityCurrForecast.getDt());
//                    temperature = -100.0;
//                } else {
//                    temperature = cityCurrForecast.getTemp();
//                }
//                forecastData.setTempByCity(city, temperature);
//            }
//            if (timestamp != currHrTimestamp) {
//                forecastDataList.add(forecastData);
//            }
//        }
        long timestamp = getCurrHourTimestamp() + 3600L;
        for (int i = 0; i < 47; i++) {
            ForecastData forecastData = new ForecastData(timestamp);
            for (String city: cities) {
                OpenWeatherForecastResponseData firstForecast = cityToForecasts.get(city).peek();
                while (firstForecast != null && firstForecast.getDt() < timestamp) {
                    cityToForecasts.get(city).removeFirst();
                    firstForecast = cityToForecasts.get(city).peek();
                }
                if (firstForecast != null && firstForecast.getDt() == timestamp) {
                    forecastData.setTempByCity(city, firstForecast.getTemp());
                    cityToForecasts.get(city).removeFirst();
                    LOGGER.log(Level.INFO, "Added temperature to forecastData for " + city + " at " + timestamp);
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
