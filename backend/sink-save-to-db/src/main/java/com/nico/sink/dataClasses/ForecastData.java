package com.nico.sink.dataClasses;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ForecastDB")
@Data
@NoArgsConstructor
public class ForecastData {
    @Id
    private String id;
    @Indexed(unique = true)
    private long dt;
    private double toronto_temp;
    private double thunder_bay_temp;
    private double ottawa_temp;
    private double timmins_temp;
}
