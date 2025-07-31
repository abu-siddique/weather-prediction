package com.weather.prediction.controller;

import com.weather.prediction.constant.Message;
import com.weather.prediction.model.WeatherResponse;
import com.weather.prediction.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class WeatherController {

    public final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(summary = "Get weather forecast for a city" )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",  description = "Successfully retrieved forecast"),
            @ApiResponse(responseCode = "401", description = "Invalid api key"),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(path = "/forecast")
    public ResponseEntity<WeatherResponse> getWeatherForecast(@RequestParam(value = "city", required = true) String city){
        try{
            log.info("Received request for city: {}", city);
            WeatherResponse wr = weatherService.getWeatherForecast(city);
            log.info("Successfully fetched");
            if(wr.getCod().equals("401")) return new ResponseEntity<>(wr, HttpStatus.UNAUTHORIZED);
            if(wr.getCod().equals("404")) return new ResponseEntity<>(wr, HttpStatus.NOT_FOUND);
            return ResponseEntity.ok(wr);
        }
        catch (Exception e) {
            log.error("Exception occurred, ", e);
            WeatherResponse wr = new WeatherResponse();
            wr.setCity(city);
            wr.setCod("500");
            wr.setMessage(Message.INTERNAL_SERVER_ERROR);
            return ResponseEntity.internalServerError().body(wr);
        }
    }

}
