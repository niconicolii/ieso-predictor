package com.nico.webfluxbackend.dataClasses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlotData {
    private Long id;
    private String dtStr;  // y-m-d-h
    private Integer demandValue;
}
