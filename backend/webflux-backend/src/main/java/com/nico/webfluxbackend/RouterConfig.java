package com.nico.webfluxbackend;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> route(WebfluxBackendHandler handler) {
        return RouterFunctions
                .route(RequestPredicates.GET("/hello"), handler::hello)
                .andRoute(RequestPredicates.GET("/dbUpdateStream"), handler::listenToDB);
    }
}
