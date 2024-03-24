package com.nico.openweathermaphistoricalweathergetter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Iterator;

@Getter
@Setter
@Document(collection = "weatherCollection")
public class WeatherData {
    @Id
    private String id;

    private String city;    // city name (correspond to lat lon)
    private long dt;  // Unix datetime value in UTC
    private long sunrise;
    private long sunset;
    private double temp;
    private int humidity;   // TODO: percentage => int or double?
    private double uvi;   //
    private int clouds;     // TODO: percentage => int or double?
    private double wind_speed;  // TODO: signiticant consider?
//    private double precipitation;   // 0 / from snow / from rain    ** forget about this... structure is a mess

    public WeatherData(String city,
                       long dt,
                       JSONObject obj) {
        this.id = city + dt;
        this.city = city;
        this.dt = dt;
        setSunriseByObj(obj);
        setSunsetByObj(obj);
        setTempByObj(obj);
        setHumidityByObj(obj);
        setUviByObj(obj);
        setCloudsByObj(obj);
        setWind_speedByObj(obj);
//        setPrecipitationByObj(obj);
    }

    public void setSunriseByObj(JSONObject obj) {
        this.sunrise = this.dt + 86399 - 1;
        if (obj.has("sunrise")){
            this.sunrise = obj.getLong("sunrise");
        }
    }

    public void setSunsetByObj(JSONObject obj) {
        this.sunset = this.dt;
        if (obj.has("set")){
            this.sunset = obj.getLong("set");
        }
    }

    public void setTempByObj(JSONObject obj) {
        this.temp = 0;
        if (obj.has("temp")){
            this.temp = obj.getInt("temp");
        }
    }

    public void setHumidityByObj(JSONObject obj) {
        this.humidity = 0;
        if (obj.has("humidity")){
            this.humidity = obj.getInt("humidity");
        }
    }

    public void setUviByObj(JSONObject obj) {
        this.uvi = 0.0;
        if (obj.has("uvi")){
            this.uvi = obj.getInt("uvi");
        }
    }

    public void setCloudsByObj(JSONObject obj) {
        this.clouds = 0;
        if (obj.has("clouds")){
            this.clouds = obj.getInt("clouds");
        }
    }

    public void setWind_speedByObj(JSONObject obj) {
        this.wind_speed = 0.0;
        if (obj.has("wind_speed")){
            this.wind_speed = obj.getInt("wind_speed");
        }
    }

//    public void setPrecipitationByObj(JSONObject obj) {
//        this.precipitation = 0.0;
//        if (obj.has("rain")){
//            this.precipitation = obj.getInt("rain");
//            obj.getJSONObject("rain").get;
//        } else if (obj.has("snow")){
//            this.precipitation = obj.getInt("snow");
//        }
//    }

    @Override
    public String toString() {
        return "WeatherData{ id='" + id + " }";
    }
}
