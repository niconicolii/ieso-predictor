package com.nico.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

@Configuration
public class SourceConfiguration {

    @Value("${ieso.source.url}")
    private String url;

    private final SourceService sourceService;

    @Autowired
    public SourceConfiguration(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @Bean
    public Supplier<String> getXMLFromIESO() {
        return () -> sourceService.getDemandXmlData(url);
    }
}
