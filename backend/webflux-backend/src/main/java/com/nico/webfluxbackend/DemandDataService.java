package com.nico.webfluxbackend;



import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Collections.singletonList;


@Service
public class DemandDataService {
    private final DemandDataRepository repository;
    private final MongoCollection<Document> collection;


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
                        Filters.in("operationType", "insert", "update", "delete")
                )
        );
        return Flux.from(collection.watch(pipeline))
                .map(changeStreamDocument -> {
                    return "New Insertion Detected!";
                });
    }

    public Flux<DemandData> getFiveMinData() {
        return repository.findAll();
    }
}
