package com.nico.webfluxbackend.urlHandler;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> route(WebfluxBackendHandler handler) {
        return RouterFunctions
                .route(RequestPredicates.GET("/hello"), handler::hello)
                .andRoute(RequestPredicates.GET("/updates"), handler::listenToDB)
                .andRoute(RequestPredicates.GET("/fiveMin"), handler::getFiveMinData)
                .andRoute(RequestPredicates.GET("/hourly"), handler::getHourlyData)
                .andRoute(RequestPredicates.GET("/daily"), handler::getDailyData)
                .andRoute(RequestPredicates.GET("/weathergy"), handler::getWEathergyData)
                .andRoute(RequestPredicates.GET("/forecast"), handler::getWeatherForecast);
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}
