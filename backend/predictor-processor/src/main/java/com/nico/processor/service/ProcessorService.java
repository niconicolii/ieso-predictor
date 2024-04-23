package com.nico.processor.service;

import com.nico.processor.dataClasses.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProcessorService {
    private final ParseXmlService parseXmlService;
    private final WeatherDataService weatherDataService;
    private final EnergyDataService energyDataService;

    @Autowired
    public ProcessorService(ParseXmlService parseXmlService,
                            WeatherDataService weatherDataService,
                            EnergyDataService energyDataService) {
        this.parseXmlService = parseXmlService;
        this.weatherDataService = weatherDataService;
        this.energyDataService = energyDataService;
    }


    public <T> List<Message<T>> wrapMessage(List<T> listToWrap) {
        List<Message<T>> messages = new ArrayList<>();
        listToWrap.forEach( item -> {
            System.out.println("Wrapping item to Message: " + item.toString());
            messages.add(MessageBuilder.withPayload(item).build());
        });
        return messages;
    }

    /////////////////// parseXml services ///////////////////
    public DemandMultidaysData parseXmlToObj(String xmlString) throws Exception {
        return parseXmlService.parseXmlToObj(xmlString);
    }

    public List<Message<DemandData>> getMessageList(DemandMultidaysData dmd, LocalDateTime maxDateTime) throws IOException {
        return parseXmlService.getMessageList(dmd, maxDateTime);
    }

    /////////////////// WEathergy Data ///////////////////
    public List<WEathergyData> createMissingWEathergyData(WEathergyMissingMessage msgInfo) throws IOException {
        return weatherDataService.createMissingWEathergyData(msgInfo);
    }

    public void fillWithDemandData(List<WEathergyData> wEathergies) throws IOException {
        energyDataService.fillDemandIntoWEathergy(wEathergies);
    }

    public List<ForecastData> getForecastFromApi(Map<String, String> cityUrls) throws IOException {
        return weatherDataService.getForecasts(cityUrls);
    }
}
