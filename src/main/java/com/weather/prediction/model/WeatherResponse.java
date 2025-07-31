package com.weather.prediction.model;

import lombok.Data;

import java.util.List;

@Data
public class WeatherResponse {
    public String cod;
    public String message;
    public String city;
    public List<ForecastData> forecastData;
}
