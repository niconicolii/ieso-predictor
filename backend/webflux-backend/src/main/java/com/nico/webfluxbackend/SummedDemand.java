package com.nico.webfluxbackend;

import lombok.Data;

@Data
public class SummedDemand {
    private String id;  // y-m-d-h
    private Double totalDemand;
}
