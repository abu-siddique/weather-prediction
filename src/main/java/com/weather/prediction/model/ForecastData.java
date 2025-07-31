package com.weather.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForecastData {

    public double min_temp = Double.MAX_VALUE;

    public double max_temp = Double.MIN_VALUE;

    public String dt_txt;

    public List<String> alerts = new ArrayList<>();
}
