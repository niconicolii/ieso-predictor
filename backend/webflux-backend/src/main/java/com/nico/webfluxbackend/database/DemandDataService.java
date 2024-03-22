package com.nico.webfluxbackend.database;



import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.nico.webfluxbackend.database.DemandData;
import com.nico.webfluxbackend.database.DemandDataRepository;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Collections.singletonList;


@Service
public class DemandDataService {
    private final DemandDataRepository repository;
    private final MongoCollection<Document> collection;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("LLL dd, yy - HH:mm");


    @Autowired
    public DemandDataService(DemandDataRepository repository,
                             MongoClient mongoClient,
                             @Value("${ieso.database}") String databaseName,
                             @Value("${ieso.collection}") String collectionName) {
        this.repository = repository;
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection(collectionName);
    }


    public Flux<String> mongoDBInsertionPublisher() {
        List<Bson> pipeline = singletonList(
                Aggregates.match(
                        Filters.in("operationType", "insert", "update", "delete")));
        return Flux.from(collection.watch(pipeline)).map(changeStreamDocument -> "New Insertion Detected!");
    }

    public Flux<PlotData> fiveMinData(String start, String end) {
        return repository.findFiveMinDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    public Flux<PlotData> hourlyData(String start, String end) {
        System.out.println(getStartDT(start) +  " ????????????????????????");
        return repository.findHourlyDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    public Flux<PlotData> dailyData(String start, String end) {
        return repository.findDailyDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    private LocalDateTime getStartDT(String dateStr) {
        if (dateStr == null) {
            return LocalDate.now().atStartOfDay();
        }
        return LocalDate.parse(dateStr).atStartOfDay();
    }

    private LocalDateTime getEndDT(String dateStr) {
        if (dateStr == null) {
            return LocalDate.now().atTime(23, 59, 59);
        }
        return LocalDate.parse(dateStr).atTime(23, 59, 59);
    }
}
