package com.nico.webfluxbackend.dataClasses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlotData {
    private String id;  // y-m-d-h
    private Double demandValue;
}
