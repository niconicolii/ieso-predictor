package com.nico.processor.dataClasses;

import jakarta.xml.bind.annotation.XmlElement;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
public class DemandData {
    @Id
    private String id;
    private double value;
    @Indexed(unique = true)
    private LocalDateTime timestamp;

    @XmlElement(name = "Value")
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}