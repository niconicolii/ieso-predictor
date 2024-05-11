package com.nico.webfluxbackend.repositories;

import com.nico.webfluxbackend.dataClasses.PlotData;
import com.nico.webfluxbackend.dataClasses.WEathergyData;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface WEathergyRepository extends ReactiveMongoRepository<WEathergyData, String> {

    @Aggregation(pipeline = {
        "{ $match: { dt: { $gte: ?0, $lte: ?1 } } }"
    })
    Flux<WEathergyData> findHourlyDemand(long start, long end);

}
