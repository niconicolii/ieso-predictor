package com.nico.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Configuration
public class SourceConfiguration {

    @Value("${ieso.source.url}")
    private String url;

    @Autowired
    private SourceService sourceService;

    @Bean
    public Supplier<String> getXMLFromIESO() {
        return () -> sourceService.getDemandXmlData(url);
    }
}
