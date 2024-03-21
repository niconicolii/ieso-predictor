package com.nico.websitebackend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data")
@CrossOrigin(origins = "*")     // TODO: 删除？
public class WebsiteBackendController {
    private final WebsiteBackendService service;

    @Autowired
    public WebsiteBackendController(WebsiteBackendService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DemandData>> getAllDemandData() {
        return new ResponseEntity<List<DemandData>>(service.allDemandData(), HttpStatus.OK);
    }

    @GetMapping("/hourly")
    public ResponseEntity<List<SumedDemand>> getSumDemandHourly() {
        return new ResponseEntity<List<SumedDemand>>(service.hourlyDemandData(), HttpStatus.OK);
    }

    @GetMapping("/daily")
    public ResponseEntity<List<SumedDemand>> getDailyDemand() {
        return new ResponseEntity<List<SumedDemand>>(service.dailyDemandData(), HttpStatus.OK);
    }

    ErrorResponse
}
