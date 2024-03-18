package com.nico.websitebackend;


import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebsiteBackendRepository extends MongoRepository<DemandData, String> {
    @Aggregation(pipeline = {
            "{ $project: { yearMonthDayHour: { $dateToString: { format: '%Y-%m-%d:%H', date: '$timestamp' } }, value: 1 } }",
            "{ $group: { _id: '$yearMonthDayHour', totalDemand: { $sum: '$value' } } }",
            "{ $sort: { _id: 1 } }"
    })
    List<SumedDemand> findHourlyDemand();

    @Aggregation(pipeline = {
            "{ $project: { yearMonthDay: { $dateToString: { format: '%Y-%m-%d', date: '$timestamp' } }, value: 1 } }",
            "{ $group: { _id: '$yearMonthDay', totalDemand: { $sum: '$value' } } }",
            "{ $sort: { _id: 1 } }"
    })
    List<SumedDemand> findDailyDemand();
}
