package com.nico.webfluxbackend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Flux;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DBInsertionEvent {
    private String eventId;
    private Flux<DemandData> demandDataList;
}
