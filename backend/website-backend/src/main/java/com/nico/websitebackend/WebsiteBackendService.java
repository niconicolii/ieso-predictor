package com.nico.websitebackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebsiteBackendService {
    @Autowired
    private WebsiteBackendRepository repository;

    public List<DemandData> allDemandData(){
        return repository.findAll();
    }

    public List<SumedDemand> hourlyDemandData() {
        return repository.findHourlyDemand();
    }

    public List<SumedDemand> dailyDemandData() {
        return repository.findDailyDemand();
    }
}
