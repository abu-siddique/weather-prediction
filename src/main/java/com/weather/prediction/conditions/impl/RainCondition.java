package com.weather.prediction.conditions.impl;

import com.google.gson.JsonObject;
import com.weather.prediction.conditions.WeatherCondition;
import org.springframework.stereotype.Component;

@Component
public class RainCondition implements WeatherCondition {
    @Override
    public boolean isApplicable(JsonObject jsonObject) {
        return  jsonObject.get("rain") != null && jsonObject.get("rain").getAsJsonObject().get("3h").getAsDouble() > 0;
    }

    @Override
    public String getAlert() {
        return "Carry umbrella";
    }
}
