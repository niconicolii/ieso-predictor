package com.nico.webfluxbackend;



import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongodb.reactivestreams.client.ChangeStreamPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;


@Service
public class DemandDataService {
    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;


    @Autowired
    public DemandDataService(MongoClient mongoClient,
                             @Value("${ieso.database}") String databaseName,
                             @Value("${ieso.collection}") String collectionName) {
        this.mongoClient = mongoClient;
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection(collectionName);
    }


    public Flux<String> mongoDBInsertionPublisher() {
        List<Bson> pipeline = singletonList(
                Aggregates.match(
                        Filters.eq("operationType", "insert")
                )
        );
        return Flux.from(collection.watch(pipeline))
                .map(changeStreamDocument -> "New Insertion Detected!!");
    }
}
