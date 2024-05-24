package com.nico.webfluxbackend.urlHandler;


import com.nico.webfluxbackend.WebfluxService;
import com.nico.webfluxbackend.dataClasses.ForecastData;
import com.nico.webfluxbackend.dataClasses.PlotData;
import com.nico.webfluxbackend.dataClasses.WEathergyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class WebfluxBackendHandler {

    @Autowired
    private WebfluxService service;


    public Mono<ServerResponse> hello(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue("Hello, Spring WebFlux!"));
    }

    public Mono<ServerResponse> listenToDB(ServerRequest request) {
        Flux<String> events = service.mongoDBInsertionPublisher();
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(events, String.class);
    }

    public Mono<ServerResponse> getFiveMinData(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(service.fiveMinData(
                        request.queryParam("start").orElse(null),
                        request.queryParam("end").orElse(null)
                ), PlotData.class);
    }


    public Mono<ServerResponse> getHourlyData(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(service.hourlyData(
                        request.queryParam("start").orElse(null),
                        request.queryParam("end").orElse(null)
                ), PlotData.class);
    }

    public Mono<ServerResponse> getDailyData(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(service.dailyData(
                        request.queryParam("start").orElse(null),
                        request.queryParam("end").orElse(null)
                ), PlotData.class);
    }

    public Mono<ServerResponse> getAllWEathergyData(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(service.allWEathergyData(), WEathergyData.class);
    }


    public Mono<ServerResponse> getConditionalWEathergy(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(service.conditionalWEathergy(
                        request.queryParam("after").orElse("0"),
                        request.queryParam("before").orElse("99999999999")
                ), WEathergyData.class);
    }

    public Mono<ServerResponse> getWeatherForecast(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(service.getForecastData(), ForecastData.class);
    }

    public Mono<ServerResponse> saveEnergyPred(ServerRequest request) {
        Mono<String> jsonBody = request.bodyToMono(String.class);
        return service.saveEnergyPredToDB(jsonBody)
                .then(ServerResponse.ok().bodyValue("Predictions saved successfully"))
                .onErrorResume(e -> ServerResponse.status(500).bodyValue("Error saving predictions to MongoDB: " + e.getMessage()));
    }

    public Mono<ServerResponse> listenToPredictionUpdates(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(service.getEnergyPredictions(), String.class);
    }

}
