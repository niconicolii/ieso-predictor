package com.nico.webfluxbackend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@Deprecated
public class MyController {
    @Autowired
    private DemandDataService service;


    public Flux<Flux<DemandData>> getInsertionNotifications() {
        // Use the service to listen to insertions and stream them to clients
        return service.mongoDBInsertionPublisher()
                .map(e -> {
                    System.out.println("Insertion Detected");
                    return service.getFiveMinData();
                });
    }

//    @GetMapping(value = "/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<DBInsertionEvent>> streamEvents() {
//        return service.mongoDBInsertionPublisher()
//                .flatMap(ignored -> {
//                    // Fetch data from repository
//                    return Mono.fromCallable(() -> service.getFiveMinData())
//                            .map(data -> new DBInsertionEvent("event-" + ignored, data));
//                })
//                .map(event -> ServerSentEvent.<DBInsertionEvent>builder()
//                        .data((DBInsertionEvent) event)
//                        .build());
//    }
//
//    @GetMapping(value = "/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<List<DemandData>>> streamEvents2() {
//        return service.mongoDBInsertionPublisher()
//                .map(e -> {
//                    List<DemandData> demandDataList = new ArrayList<>();
//                    service.getFiveMinData()
//                            .subscribe(demandDataList::add);
//                    System.out.println("Insertion Detected!");
////                    return ServerSentEvent.<List<DemandData>>builder()
////                            .data(demandDataList)
////                            .build();
//                    return demandDataList;
//                })
//                .map(demandDataList -> ServerSentEvent.<List<DemandData>>builder()
//                            .data(demandDataList)
//                            .build());
//    }
}
