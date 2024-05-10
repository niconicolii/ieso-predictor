package com.nico.webfluxbackend.dataClasses;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "demandData")
@Getter
@Setter
public class DemandData {
    @Id
    private String id;
    @Indexed(unique = true)
    private LocalDateTime timestamp;
    private double value;
}
