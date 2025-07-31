package com.weather.prediction.conditions.impl;

import com.google.gson.JsonObject;
import com.weather.prediction.conditions.WeatherCondition;
import org.springframework.stereotype.Component;

@Component
public class HighWindCondition implements WeatherCondition {
    @Override
    public boolean isApplicable(JsonObject jsonObject) {
        double speed = jsonObject.get("wind").getAsJsonObject().get("speed").getAsDouble();
        return speed  > 4.47;  //mph to m/s
    }

    @Override
    public String getAlert() {
        return "It’s too windy, watch out!";
    }
}
