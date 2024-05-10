package com.nico.webfluxbackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nico.webfluxbackend.dataClasses.EnergyPredData;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class EnergyPredictionSSEPublisher {
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publishFlux(Flux<EnergyPredData> flux) {
        flux.flatMap(this::serializeToJson)
            .subscribe(
                sink::tryEmitNext,
                error -> sink.tryEmitNext(String.valueOf(error)),
                sink::tryEmitComplete
            );
    }

    private Mono<String> serializeToJson(EnergyPredData data){
        try {
            String json = new ObjectMapper().writeValueAsString(data)
            return Mono.just(json);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    public Flux<String> getStream() {
        return sink.asFlux();
    }
}
