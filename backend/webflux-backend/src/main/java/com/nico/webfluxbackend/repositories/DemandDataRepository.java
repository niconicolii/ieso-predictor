package com.nico.webfluxbackend.repositories;

import com.nico.webfluxbackend.dataClasses.DemandData;
import com.nico.webfluxbackend.dataClasses.PlotData;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface DemandDataRepository extends ReactiveMongoRepository<DemandData, String> {

    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $project: { _id: '$timestamp', dtStr: { $dateToString: { format: '%b %d, %Y - %H:%M', date: '$timestamp', timezone: 'America/New_York' } }, demandValue: '$value' } }",
            "{ $sort: { _id: 1 } }"
    })
    Flux<PlotData> findFiveMinDemandToPlotData(LocalDateTime start, LocalDateTime end);

    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $group: { _id: null, valueAvg: { $avg: '$value' } } }",
            "{ $project: { roundedAvg: { $round: [{ $ifNull: ['$valueAvg', 0] }, 0] } } }"
    })
    Mono<Integer> findFiveMinAvg(LocalDateTime start, LocalDateTime end);

    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $project: { yearMonthDayHour: { $dateToString: { format: '%b %d, %Y - %H:00', date: '$timestamp', timezone: 'America/New_York' } }, value: 1 } }",
            "{ $group: { _id: '$timestamp', dtStr: '$yearMonthDayHour', demandValue: { $sum: '$value' } } }",
            "{ $sort: { _id: 1 } }"
    })
    Flux<PlotData> findHourlyDemand(LocalDateTime start, LocalDateTime end);


    @Aggregation(pipeline = {
            "{ $match: { timestamp: { $gte: ?0, $lte: ?1 } } }",
            "{ $project: { yearMonthDay: { $dateToString: { format: '%b %d, %Y', date: '$timestamp', timezone: 'America/New_York' } }, value: 1 } }",
            "{ $group: { _id: '$timestamp', dtStr: '$yearMonthDay', demandValue: { $sum: '$value' } } }",
            "{ $sort: { _id: 1 } }"
    })
    Flux<PlotData> findDailyDemand(LocalDateTime start, LocalDateTime end);

}
