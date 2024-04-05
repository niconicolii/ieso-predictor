package com.nico.processor.dataClasses;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class WEathergyMissingMessage implements Serializable {
    private List<Long> dts;
    private Map<String, String> cityToWeatherCsvUrl;
    private Map<String, String> cityToWeatherApiUrlPrefix;
    private String iesoUrl;

    public WEathergyMissingMessage() {
        this.dts = new ArrayList<>();
        this.cityToWeatherCsvUrl = new HashMap<>();
        this.cityToWeatherApiUrlPrefix = new HashMap<>();
    }
}
