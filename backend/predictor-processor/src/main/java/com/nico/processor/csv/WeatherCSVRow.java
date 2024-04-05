package com.nico.processor.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
public class WeatherCSVRow {
    @CsvBindByName(column = "Year")
    private int year;
    @CsvBindByName(column = "Month")
    private int month;
    @CsvBindByName(column = "Day")
    private int day;
    @CsvBindByName(column = "Time (LST)")
    private String time;
    @CsvBindByName(column = "Temp (°C)", required = false)
    private double temp = -1000.0;

    private long dt;

    @Override
    public String toString() {
        return String.format("{ Date: %d-%d-%d, Time: %s, Temp: %f°C }",
                this.year,
                this.month,
                this.day,
                this.time,
                this.temp);
    }

    public long getUnixTime() {
        if (this.dt != 0) {
            return this.dt;
        }
        int hour = Integer.parseInt(this.time.split(":")[0]);
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, 0);
        ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/New_York"));
        Instant instant = zdt.toInstant();
        this.dt = instant.getEpochSecond();
        return this.dt;
    }
}
