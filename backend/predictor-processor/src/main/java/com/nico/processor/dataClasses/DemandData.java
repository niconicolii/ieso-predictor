package com.nico.processor.dataClasses;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
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
}
