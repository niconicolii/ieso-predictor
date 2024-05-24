package com.nico.webfluxbackend.repositories;

import com.nico.webfluxbackend.dataClasses.WEathergyData;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface WEathergyRepository extends ReactiveMongoRepository<WEathergyData, String> {

    @Aggregation(pipeline = {
        "{ $match: { dt: { $gte: ?0, $lte: ?1 } } }",
        "{ $sort: { dt: 1 } }"
    })
    Flux<WEathergyData> findWeathergyDemandInclusive(long start, long end);

    @Aggregation(pipeline = {
            "{ $match: { dt: { $gt: ?0, $lt: ?1 } } }",
            "{ $sort: { dt: 1 } }"
    })
    Flux<WEathergyData> findWeathergyDemandExclusive(long after, long before);
}
