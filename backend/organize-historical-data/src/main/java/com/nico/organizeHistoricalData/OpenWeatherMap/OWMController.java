//package com.nico.organizeHistoricalData.OpenWeatherMap;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@RestController
//@Deprecated
//public class OWMController {
//    private final DateTimeFormatter formatter;
//    private final OWMHistoricalWeatherService service;
//
//    public OWMController(OWMHistoricalWeatherService service) {
//        this.service = service;
//        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//    }
//
//    @GetMapping("/plainhistory")
//    public String getHistoryPlain(@RequestParam("city") String cityName,
//                                  @RequestParam("dt") long dt) {
//        return service.getFromApi(cityName, dt);
//    }
//
//    @GetMapping("/history")
//    public String getHistory(@RequestParam("city") String cityName,
//                             @RequestParam("dt") long dt,
//                             @RequestParam(name="todb", defaultValue= "true") String toDb) {
//        WeatherData weatherData = service.createWeatherDataByApiCall(cityName, dt);
//        if (!toDb.equals("false")) {
//            service.insertWeatherData(weatherData);
//            return "Created " + weatherData + " and added into MongoDB";
//        }
//        return "Created " + weatherData;
//    }
//
//    @GetMapping("/histories")
//    public String getHistories(@RequestParam("city") String cityName,
//                               @RequestParam("dt") long dt,
//                               @RequestParam("count") int count) {
//        for (int i = 0; i < count; i++) {
//            WeatherData weatherData = service.createWeatherDataByApiCall(cityName, dt + i * 3600L);
//            service.insertWeatherData(weatherData);
//            System.out.println("Created " + weatherData + " and added into MongoDB");
//        }
//        LocalDateTime localStart = LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneId.systemDefault());
//        LocalDateTime localEnd = LocalDateTime.ofInstant(Instant.ofEpochSecond(dt + (count-1) * 3600L), ZoneId.systemDefault());
//        return "Added " + count + " historical values from " + localStart.format(formatter) + "  to  " + localEnd.format(formatter);
//    }
//
//    @GetMapping("/reformat")
//    public String reformatWeatherDataID() {
//        service.updateId();
//        return "Re-Formatted";
//    }
//
//    @GetMapping("/check_if_missing")
//    public List<String> checkIfRecordMissing() {
//        return service.getRecordMissing();
//    }
//}
