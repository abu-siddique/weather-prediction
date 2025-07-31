package com.weather.prediction.conditions.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.weather.prediction.conditions.WeatherCondition;
import org.springframework.stereotype.Component;

@Component
public class ThunderstormCondition implements WeatherCondition {
    @Override
    public boolean isApplicable(JsonObject jsonObject) {
        JsonArray weather = jsonObject.getAsJsonArray("weather");
        return  weather != null &&
                weather.asList().stream().
                        anyMatch(w -> w.getAsJsonObject().get("main").getAsString().contains("Thunderstorm"));
    }

    @Override
    public String getAlert() {
        return "Don’t step out! A Storm is brewing!";
    }
}
