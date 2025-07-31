package com.weather.prediction.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.weather.prediction.conditions.WeatherCondition;
import com.weather.prediction.constant.Message;
import com.weather.prediction.exception.CustomRuntimeException;
import com.weather.prediction.model.ForecastData;
import com.weather.prediction.model.WeatherResponse;
import com.weather.prediction.utils.ApiUtils;
import io.swagger.v3.core.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WeatherService {

    List<WeatherCondition> weatherConditions;

    ApiUtils apiUtils;

    @Value("${spring.weather.api.url}")
    public String url;

    @Value("${spring.weather.api.key}")
    public String key;

    @Value("${spring.weather.api.cnt}")
    public String cnt;

    public WeatherService(List<WeatherCondition> weatherConditions, ApiUtils apiUtils) {
        this.weatherConditions = weatherConditions;
        this.apiUtils = apiUtils;
    }

    /**
     * Retrieves weather forecast data for a specified city by calling an external API.
     * Processes the response to extract daily min/max temperatures and weather alerts.
     * Handles various error scenarios and returns appropriate response codes/messages.
     *
     * @param city The name of the city for which to retrieve weather forecast
     * @return WeatherResponse object containing:
     *         - City name
     *         - Forecast data (list of ForecastData objects with min/max temps and alerts)
     *         - Response code ("200" for success, error codes for failures)
     *         - Message (error message if applicable)
     * @throws CustomRuntimeException if an unexpected runtime error occurs during API call
     */
    public WeatherResponse getWeatherForecast(String city){

        String req = url + "?q=" + city + "&appid=" + key + "&cnt=" + cnt;
        WeatherResponse wr =  new WeatherResponse();
        wr.setCity(city);

        String res = null;
        try{
            res = apiUtils.callGetApiWithRetry(req);
        }catch (RestClientResponseException e){
           String resBody = e.getResponseBodyAsString();
           JsonObject jsonObject = new Gson().fromJson(resBody, JsonObject.class);
           wr.setCod(jsonObject.get("cod").getAsString());
           wr.setMessage(jsonObject.get("message").getAsString());
           return wr;
        }catch (ResourceAccessException e){
            log.warn("Resource Access Exception Occurred: ", e);
            wr.setCod("503");
            wr.setMessage(Message.SERVICE_UNAVAILABLE);
            return wr;
        }catch (RuntimeException e){
            throw new CustomRuntimeException(String.format("A runtime exception occurred while invoking apiUtils.callGetApiWithRetry: %s", e.getMessage()), e.getCause());
        }
        JsonObject jsonObject = new Gson().fromJson(res, JsonObject.class);
        wr.setCod(jsonObject.get("cod").getAsString());
        wr.setMessage(jsonObject.get("message").getAsString());

        JsonArray list = jsonObject.getAsJsonArray("list");
        if(list == null){
           return wr;
        }

        Map<String, ForecastData> forecastMap = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        list.forEach(jsonElement -> {
            JsonObject jo = jsonElement.getAsJsonObject();
            String dateStr = dateFormat.format(new Date(jo.get("dt").getAsLong() * 1000));

            ForecastData dayData = forecastMap.computeIfAbsent(dateStr, k -> new ForecastData());
            double temp =  jo.get("main").getAsJsonObject().get("temp").getAsDouble();
            dayData.setMax_temp(Math.max(dayData.getMax_temp(), temp));
            dayData.setMin_temp(Math.min(dayData.getMin_temp(), temp));
            this.updateAlerts(jo, dayData);


        });

        List<ForecastData> forecastData = forecastMap.entrySet().
                                    stream().sorted(Map.Entry.comparingByKey()).
                                    limit(4).
                                    map(entry -> new ForecastData(
                                            Math.round(entry.getValue().getMin_temp()),
                                            Math.round(entry.getValue().getMax_temp()) ,
                                            entry.getKey(),
                                            entry.getValue().getAlerts())).
                                    skip(1).
                                    toList();



        wr.setForecastData(forecastData);
        return wr;
    }

    private void updateAlerts(JsonObject jsonObject, ForecastData dayData){
        List<String> alerts = dayData.getAlerts();
        for(WeatherCondition wc : weatherConditions){
            if(wc.isApplicable(jsonObject)){
                String alert = wc.getAlert();
                if(!alerts.contains(alert)) alerts.add(alert);
            }
        }
    }


}
