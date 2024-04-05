package com.nico.processor.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class EnergyCSVRow {
    @CsvBindByName(column = "Date")
    private String date;
    @CsvBindByName(column = "Hour")
    private int hour;
    @CsvBindByName(column = "Ontario Demand")
    private int demand;

    private long dt;

    public long getDt() {
        if (dt <= 0.0) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(this.date, dateTimeFormatter);
            if (this.hour == 24) this.hour = 0;
            LocalDateTime ldt = date.atTime(this.hour, 0);

            ZoneId zoneId = ZoneId.of("America/New_York");
            ZonedDateTime zdt = ldt.atZone(zoneId);

            this.dt = zdt.toEpochSecond();
        }
        return this.dt;
    }

    @Override
    public String toString() {
        return String.format("{ date = %s ; hour = %d ; demand = %d }", date, hour, demand);
    }
}
