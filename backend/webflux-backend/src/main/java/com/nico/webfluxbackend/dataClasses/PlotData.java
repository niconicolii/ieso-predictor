package com.nico.webfluxbackend.dataClasses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class PlotData {
    private Long id;
    private String dtStr;  // y-m-d-h
    private Integer demandValue;

    public PlotData(Long id, String dtStr, Integer demandValue) {
        this.id = id > 99999999999L ? id / 1000 : id;
        this.dtStr = dtStr;
        this.demandValue = demandValue;
    }
}
