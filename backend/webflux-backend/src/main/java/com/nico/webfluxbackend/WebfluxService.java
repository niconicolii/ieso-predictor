package com.nico.webfluxbackend;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.nico.webfluxbackend.dataClasses.EnergyPredData;
import com.nico.webfluxbackend.dataClasses.ForecastData;
import com.nico.webfluxbackend.dataClasses.PlotData;
import com.nico.webfluxbackend.dataClasses.WEathergyData;
import com.nico.webfluxbackend.repositories.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.singletonList;


@Service
public class WebfluxService {
    private final DemandDataRepository demandDataRepository;
    private final WEathergyRepository wEathergyRepository;
    private final ForecastDataRepository forecastRepository;
    private final EnergyPredRepository energyPredRepository;
    private final MongoCollection<Document> demandCollection;
    private final EnergyPredictionSSEPublisher energySSEPublisher;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("LLL dd, yy - HH:mm");
    private static final Logger LOGGER = Logger.getLogger(WebfluxService.class.getName());
    private final ZoneId zoneId = ZoneId.of("America/New_York");


    @Autowired
    public WebfluxService(DemandDataRepository demandDataRepository,
                          WEathergyRepository wEathergyRepository,
                          ForecastDataRepository forecastRepository,
                          EnergyPredRepository energyPredRepository,
                          EnergyPredictionSSEPublisher energySSEPublisher,
                          MongoClient mongoClient,
                          @Value("${ieso.database}") String databaseName,
                          @Value("${ieso.demandCollection}") String demandCollectionName, ) {
        this.demandDataRepository = demandDataRepository;
        this.wEathergyRepository = wEathergyRepository;
        this.forecastRepository = forecastRepository;
        this.energyPredRepository = energyPredRepository;
        this.energySSEPublisher = energySSEPublisher;
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.demandCollection = database.getCollection(demandCollectionName);
    }


    // Listen to updates in MongoDB
    public Flux<String> mongoDBInsertionPublisher() {
        List<Bson> pipeline = singletonList(
                Aggregates.match(
                        Filters.in("operationType", "insert", "update", "delete")));
        return Flux.from(demandCollection.watch(pipeline)).map(changeStreamDocument -> "New Insertion Detected!");
    }

    // get five-minute energy demand data from demandData repository
    public Flux<PlotData> fiveMinData(String start, String end) {
        return demandDataRepository.findFiveMinDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    // get accumulated hourly energy demand data which is calculated using five-minute data from demandData repository
    public Flux<PlotData> hourlyData(String start, String end) {
        return demandDataRepository.findHourlyDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    // get accumulated daily energy demand data which is calculated using five-minute data from demandData repository
    public Flux<PlotData> dailyData(String start, String end) {
        return demandDataRepository.findDailyDemand(
                getStartDT(start),
                getEndDT(end)
        );
    }

    // convert string representation of a datetime into LocalDateTime variable
    // if string is not given, return today's datetime at start of day
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


    // convert string representation of datetime into LocalDateTime variable
    // if string is not given, return today's datetime at end of day
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

    // get all WEathergyData from MongoDB
    public Flux<WEathergyData> wEathergyData() {
        return wEathergyRepository.findAll();
    }

    // get all forecast data from MongoDB
    public Flux<ForecastData> getForecastData() {
        return forecastRepository.findAll();
    }

    public Mono<Void> saveEnergyPredToDB(Mono<String> stringMono) {
        return stringMono.flatMap(str -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Map<String, Integer> map = objectMapper.readValue(str, Map.class);
                return processPredictions(map);
            } catch (JsonProcessingException e) {
                return Mono.error(new RuntimeException("Could not read energy predictions received from python", e));
            }
        });
    }

    private Mono<Void> processPredictions(Map<String, Integer> timestampToPreds) {
        return energyPredRepository.deleteAll()
            .doOnSuccess(blah ->
                LOGGER.log(Level.INFO, "[INFO] Delete all energy demand predictions for new prediction set.")
            ).thenMany(
                Flux.fromIterable(timestampToPreds.entrySet())
                    .flatMap(entry -> {
                        LocalDateTime dt = Instant.ofEpochSecond(Long.parseLong(entry.getKey())).atZone(zoneId).toLocalDateTime();
                        EnergyPredData pred = new EnergyPredData(dt, entry.getValue());
                        return energyPredRepository.insert(pred);
                    })
            )
            .then(
                    Mono.fromRunnable(() -> {
                        LOGGER.log(Level.INFO, "[INFO] Updated " + energyPredRepository.count() + " new energy demand predictions.");
                        Flux<EnergyPredData> preds = energyPredRepository.findAll();
                        energySSEPublisher.publishFlux(preds);
                    })
            );
    }

    public Flux<String> getEnergyPredictions() {
        return energySSEPublisher.getStream();
    }
}
