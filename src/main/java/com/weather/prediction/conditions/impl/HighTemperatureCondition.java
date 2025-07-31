package com.weather.prediction.conditions.impl;

import com.google.gson.JsonObject;
import com.weather.prediction.conditions.WeatherCondition;
import org.springframework.stereotype.Component;

@Component
public class HighTemperatureCondition implements WeatherCondition {
    @Override
    public boolean isApplicable(JsonObject jsonObject) {
        double temp = jsonObject.get("main").getAsJsonObject().get("temp").getAsDouble();
        return temp - 273.15 > 40 ; //kelvin to celsius
    }

    @Override
    public String getAlert() {
        return "Use sunscreen lotion";
    }
}
