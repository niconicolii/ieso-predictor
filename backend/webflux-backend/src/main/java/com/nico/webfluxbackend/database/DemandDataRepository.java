package com.nico.webfluxbackend.database;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface DemandDataRepository extends ReactiveMongoRepository<DemandData, String> {

    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $project: { _id: { $dateToString: { format: '%b %d, %Y - %H:%M', date: '$timestamp', timezone: 'America/New_York' } }, demandValue: '$value' } }",
            "{ $sort: { _id: 1 } }"
    })
    Flux<PlotData> findFiveMinDemand(LocalDateTime start, LocalDateTime end);

    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $project: { yearMonthDayHour: { $dateToString: { format: '%b %d, %Y - %H:00', date: '$timestamp', timezone: 'America/New_York' } }, value: 1 } }",
            "{ $group: { _id: '$yearMonthDayHour', demandValue: { $sum: '$value' } } }",
            "{ $sort: { _id: 1 } }"
    })
    Flux<PlotData> findHourlyDemand(LocalDateTime start, LocalDateTime end);

    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $project: { yearMonthDay: { $dateToString: { format: '%b %d, %Y', date: '$timestamp', timezone: 'America/New_York' } }, value: 1 } }",
            "{ $group: { _id: '$yearMonthDay', demandValue: { $sum: '$value' } } }",
            "{ $sort: { _id: 1 } }"
    })
    Flux<PlotData> findDailyDemand(LocalDateTime start, LocalDateTime end);

    Flux<DemandData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

}
