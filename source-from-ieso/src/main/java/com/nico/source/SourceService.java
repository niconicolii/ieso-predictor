package com.nico.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// perform HTTP GET request from IESO
@Service
public class SourceService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getDemandXmlData(String url) {
        return restTemplate.getForObject(url, String.class);
    }
}
