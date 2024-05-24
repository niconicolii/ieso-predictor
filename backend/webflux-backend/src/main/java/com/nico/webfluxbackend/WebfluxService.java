package com.nico.webfluxbackend;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.nico.webfluxbackend.dataClasses.*;
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
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yy - HH:mm");
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
                          @Value("${ieso.demandCollection}") String demandCollectionName) {
        this.demandDataRepository = demandDataRepository;
        this.wEathergyRepository = wEathergyRepository;
        this.forecastRepository = forecastRepository;
        this.energyPredRepository = energyPredRepository;
        this.energySSEPublisher = energySSEPublisher;
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.demandCollection = database.getCollection(demandCollectionName);
    }

    // Get the epoch time of when IESO Public Repository last updated:
    // if it's before 8AM, the last update is at end of the day before yesterday == today at 00:00:00 ;
    // if it's after 8AM, the last update is at the end of yesterday == yesterday at 00:00:00 .
    private long getPubRepoLastUpdateEpoch() {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        long lastEpoch = now.withHour(0).withMinute(0).withSecond(0).toEpochSecond();
        if (now.getHour() >= 8) {
            return lastEpoch;
        }
        return lastEpoch - (3600L * 24);
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
        return demandDataRepository.findFiveMinDemandToPlotData(
                getStartDT(start),
                getEndDT(end)
        );
    }

    // get hourly energy demand data from IESO public repository,
    // data for hours that is not yet updated in IESO public repository will be calculated using five-min data
    public Flux<PlotData> hourlyData(String start, String end) {
        LocalDateTime startDT = getStartDT(start);
        LocalDateTime endDT = getEndDT(end);
        long endTimestamp = endDT.atZone(zoneId).toEpochSecond();

        Flux<WEathergyData> hourlyDemand = wEathergyRepository.findWeathergyDemandInclusive(
                startDT.atZone(zoneId).toEpochSecond(),
                endTimestamp
        );

        return hourlyDemand.flatMap(data -> {
            LocalDateTime ldt = Instant.ofEpochSecond(data.getDt()).atZone(zoneId).toLocalDateTime();
            String dtStr = ldt.format(dateTimeFormatter);
            // since WEathergyDB will have all up-to-date documents (including document for very last hour
            // with demand=0), calculate demand for this document
            if (data.getDemand() <= 0) {
                // get five-min data within 60 minutes
                LocalDateTime fromEpoch = ZonedDateTime
                        .ofInstant(Instant.ofEpochSecond(data.getDt()), zoneId)
                        .toLocalDateTime();
                return demandDataRepository
                        .findFiveMinAvg(fromEpoch.minusMinutes(30), fromEpoch.plusMinutes(25))
                        .map(calculatedDemand -> new PlotData(data.getDt(), dtStr, calculatedDemand));
            }
            return Mono.just(new PlotData(data.getDt(), dtStr, data.getDemand()));
        });
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
            return LocalDate.now(zoneId).atStartOfDay();
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
            return LocalDate.now(zoneId).atTime(23, 59, 59);
        }
        try {
            return LocalDateTime.parse(dateStr);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(dateStr).atTime(23, 59, 59);
        }
    }

    // get all WEathergyData from MongoDB
    public Flux<WEathergyData> allWEathergyData() {
        return wEathergyRepository.findAll();
    }


    public Flux<WEathergyData> conditionalWEathergy(String after, String before) {
        return wEathergyRepository.findWeathergyDemandExclusive(Long.parseLong(after), Long.parseLong(before));
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
                LOGGER.log(Level.INFO, "[INFO] Deleted all energy demand predictions for new prediction set.")
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
