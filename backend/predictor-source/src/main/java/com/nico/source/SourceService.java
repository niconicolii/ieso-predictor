package com.nico.source;

import com.nico.source.configuration.CityProperties;
import com.nico.source.dataClasses.DemandData;
import com.nico.source.dataClasses.WEathergyMissingMessage;
import com.nico.source.repository.DemandDataRepository;
import com.nico.source.dataClasses.WEathergyData;
import com.nico.source.repository.WEathergyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.util.*;

// perform HTTP GET request from IESO
@Service
public class SourceService {
    private final Map<String, Map<String, Number>> cityProperties;
    private final List<String> cityNames;
    private final DemandDataRepository demandDataRepository;
    private final WEathergyRepository wEathergyRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String weaCsvFmt = "https://climate.weather.gc.ca/climate_data/bulk_data_e.html?format=csv&stationID=%d&Year=%d&Month=%d&Day=14&timeframe=1&submit=Data";
    private final String weaApiFmt = "https://api.openweathermap.org/data/3.0/onecall/timemachine?lat=%f&lon=%f&units=metric&dt=";
    private final ZoneId zoneId = ZoneId.of("America/New_York");

    @Autowired
    public SourceService(CityProperties cityProperties,
                         DemandDataRepository demandDataRepository,
                         WEathergyRepository wEathergyRepository) {
        this.cityProperties = cityProperties.getCities();
        this.cityNames = this.cityProperties.keySet().stream().toList();
        this.demandDataRepository = demandDataRepository;
        this.wEathergyRepository = wEathergyRepository;
    }

    public String getDemandXmlData(String url) {
        return restTemplate.getForObject(url, String.class);
    }

    public String getMaxDateTimeStr() {
        LocalDateTime maxDateTime;
        Optional<DemandData> optional = demandDataRepository.findTopByOrderByTimestampDesc();
        if (optional.isPresent()) {
            maxDateTime =  optional.get().getTimestamp();
            return maxDateTime.toString();
        }
        return LocalDateTime.MIN.toString();
    }

    public WEathergyMissingMessage constructNewMissingMsg(int year, int month) {
        WEathergyMissingMessage currMsg = new WEathergyMissingMessage();
        Map<String, String> cityToWeatherCsvUrl = new HashMap<>();
        Map<String, String> cityToWeatherAPIUrl = new HashMap<>();
        for (String city : cityNames) {
            Map<String, Number> cityInfo = cityProperties.get(city);
            cityToWeatherCsvUrl.put(
                    city,
                    String.format(weaCsvFmt, cityInfo.get("station").intValue(), year, month));
            cityToWeatherAPIUrl.put(
                    city,
                    String.format(weaApiFmt, cityInfo.get("lat").doubleValue(), cityInfo.get("lon").doubleValue()));
        }
        currMsg.setCityToWeatherCsvUrl(cityToWeatherCsvUrl);
        currMsg.setCityToWeatherApiUrlPrefix(cityToWeatherAPIUrl);
        currMsg.setIesoUrl("http://reports.ieso.ca/public/Demand/" + "PUB_Demand_" + year + ".csv");
        return currMsg;
    }

    public List<WEathergyMissingMessage> checkIfWEathergyDatabaseComplete() {
        Map<String, WEathergyMissingMessage> yearMonthToMissingMsg = new HashMap<>();

        long dt = LocalDate.of(2022, 1, 1).atStartOfDay().atZone(zoneId).toEpochSecond();
        long now = Instant.now().getEpochSecond();
        int i = 0;
        List<WEathergyData> allSorted = wEathergyRepository.findAllByOrderByDtAsc();
        while (dt < now) {
            // conditions to do nothing: dt found a match && (data complete || on missing value that is allowed to miss)
            //      => iterate i and dt
            // otherwise: need to send to processor
            //      => iterate dt, iterate i if found match
            if (i < allSorted.size()) {
                WEathergyData data = allSorted.get(i);
                long dataDt = data.getDt();
                if (dataDt == dt && !data.missingWeatherValue() && validDemandValue(data) ) {
                    i++;
                } else {
                    addNewMissingMsgToMap(dt, yearMonthToMissingMsg);
                    i = (dataDt == dt) ? i+1 : i;
                }
            } else {
                addNewMissingMsgToMap(dt, yearMonthToMissingMsg);
            }
            dt += 3600L;
        }
        return yearMonthToMissingMsg.values().stream().toList();
    }

    private void addNewMissingMsgToMap(
            long dt,
            Map<String, WEathergyMissingMessage> msgMap) {
        LocalDateTime ldt = Instant.ofEpochSecond(dt).atZone(zoneId).toLocalDateTime();
        String mapKey = String.format("%d-%d", ldt.getYear(), ldt.getMonthValue());
        if (msgMap.containsKey(mapKey)) {
            msgMap.get(mapKey).getDts().add(dt);
        } else {
            WEathergyMissingMessage newMsg = constructNewMissingMsg(ldt.getYear(), ldt.getMonthValue());
            msgMap.put(mapKey, newMsg);
        }
    }


    private boolean validDemandValue(WEathergyData data) {
        if (data.getDemand() > 0) {
            return true;
        }
        LocalDate dataDate = Instant.ofEpochSecond(data.getDt()).atZone(zoneId).toLocalDate();
        // if before eight, allow missing demand value if dt is yesterday and after
        if (LocalDateTime.now().getHour() <= 8) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            return dataDate.equals(yesterday) || dataDate.isAfter(yesterday);
        }
        // Allow missing demand value if dt is today
        return dataDate.equals(LocalDate.now());
    }

    public WEathergyMissingMessage constructNewMissingMsgForYesterday() {
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atTime(1, 0);
        WEathergyMissingMessage missingMessage = constructNewMissingMsg(yesterday.getYear(), yesterday.getMonthValue());
        long dt = yesterday.atZone(zoneId).toEpochSecond();
        long dtEnd = LocalDate.now().atTime(0,0).atZone(zoneId).toEpochSecond();
        while(dt <= dtEnd) {
            missingMessage.getDts().add(dt);
            dt += 3600L;
        }
        return missingMessage;

    }
}
