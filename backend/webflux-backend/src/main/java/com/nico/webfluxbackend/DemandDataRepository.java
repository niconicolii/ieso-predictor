package com.nico.webfluxbackend;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface DemandDataRepository extends ReactiveMongoRepository<DemandData, String> {
    @Aggregation(pipeline = {
            "{ $project: { yearMonthDayHour: { $dateToString: { format: '%Y-%m-%d:%H', date: '$timestamp' } }, value: 1 } }",
            "{ $group: { _id: '$yearMonthDayHour', totalDemand: { $sum: '$value' } } }",
            "{ $sort: { _id: 1 } }"
    })
    Flux<SummedDemand> findHourlyDemand();

    @Aggregation(pipeline = {
            "{ $project: { yearMonthDay: { $dateToString: { format: '%Y-%m-%d', date: '$timestamp' } }, value: 1 } }",
            "{ $group: { _id: '$yearMonthDay', totalDemand: { $sum: '$value' } } }",
            "{ $sort: { _id: 1 } }"
    })
    Flux<SummedDemand> findDailyDemand();

}
