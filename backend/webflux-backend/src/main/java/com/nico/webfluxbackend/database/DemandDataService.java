package com.nico.webfluxbackend.database;



import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static java.util.Collections.singletonList;


@Service
public class DemandDataService {
    private final DemandDataRepository demandDataRepository;
    private final WEathergyRepository wEathergyRepository;
    private final MongoCollection<Document> demandCollection;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("LLL dd, yy - HH:mm");


    @Autowired
    public DemandDataService(DemandDataRepository demandDataRepository,
                             MongoClient mongoClient,
                             @Value("${ieso.database}") String databaseName,
                             @Value("${ieso.demandCollection}") String demandCollectionName,
                             WEathergyRepository wEathergyRepository ) {
        this.demandDataRepository = demandDataRepository;
        this.wEathergyRepository = wEathergyRepository;
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.demandCollection = database.getCollection(demandCollectionName);
    }


    public Flux<String> mongoDBInsertionPublisher() {
        List<Bson> pipeline = singletonList(
                Aggregates.match(
                        Filters.in("operationType", "insert", "update", "delete")));
        return Flux.from(demandCollection.watch(pipeline)).map(changeStreamDocument -> "New Insertion Detected!");
    }

    public Flux<PlotData> fiveMinData(String start, String end) {
        return demandDataRepository.findFiveMinDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    public Flux<PlotData> hourlyData(String start, String end) {
        return demandDataRepository.findHourlyDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    public Flux<PlotData> dailyData(String start, String end) {
        return demandDataRepository.findDailyDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    private LocalDateTime getStartDT(String dateStr) {
        if (dateStr == null) {
            return LocalDate.now().atStartOfDay();
        }
        try {
            return LocalDateTime.parse(dateStr);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(dateStr).atStartOfDay();
        }
    }

    private LocalDateTime getEndDT(String dateStr) {
        if (dateStr == null) {
            return LocalDate.now().atTime(23, 59, 59);
        }
        try {
            return LocalDateTime.parse(dateStr);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(dateStr).atTime(23, 59, 59);
        }
    }

    public Flux<WEathergyData> wEathergyData() {
        return wEathergyRepository.findAll();
    }
}
