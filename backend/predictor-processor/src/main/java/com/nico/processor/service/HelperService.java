package com.nico.processor.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class HelperService {
    private ZoneId zoneId = ZoneId.of("America/New_York");

    public int dtToYear (long dt) {
        LocalDateTime ldt = Instant.ofEpochSecond(dt).atZone(zoneId).toLocalDateTime();
        return ldt.getYear();
    }

    public LocalDateTime dtToLdt(long dt) {
        return Instant.ofEpochSecond(dt).atZone(zoneId).toLocalDateTime();
    }

    public LocalDate dtToLocalDate(long dt) {
        return Instant.ofEpochSecond(dt).atZone(zoneId).toLocalDate();
    }

    public long localYmdtToDT(int year, int month, int day, int hour) {
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, 0);
        return ldt.atZone(zoneId).toEpochSecond();
    }

    public long getTodayAtHourDt(int hour) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayAtHour = today.atTime(hour, 0);
        return todayAtHour.atZone(zoneId).toEpochSecond();
    }

}
