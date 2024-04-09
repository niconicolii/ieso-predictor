//package com.nico.organizeHistoricalData.OpenWeatherMap;
//
//import org.springframework.data.mongodb.repository.Aggregation;
//import org.springframework.data.mongodb.repository.MongoRepository;
//
//import java.util.List;
//@Deprecated
//public interface OWMHistoricalWeatherRepository extends MongoRepository<WeatherData, String> {
//
//    @Aggregation(pipeline = {
//            "{ '$sort' : { 'dt' : 1 } }",
//            "{ '$group' : { '_id' : '$city', 'count' : { '$sum' : 1 }, 'weatherList': { '$push' : '$$ROOT' } } }",
//            "{ '$project' : { 'city' : '$_id', '_id' : 0, 'count' : 1, 'weatherList' : 1 } }"
//    })
//    List<CityWeather> groupWeatherByCity();
//}
