package com.weather.prediction.conditions;

import com.google.gson.JsonObject;

public interface WeatherCondition {

    public boolean isApplicable(JsonObject jsonObject);

    public String getAlert();

}
