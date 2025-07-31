package com.weather.prediction.controller.test;

import com.weather.prediction.constant.Message;
import com.weather.prediction.controller.WeatherController;
import com.weather.prediction.model.WeatherResponse;
import com.weather.prediction.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeatherControllerTest {

    private WeatherController controller;

    @Mock
    private WeatherService weatherService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        controller = new WeatherController(weatherService);
    }

    @Test
    void testGetWeatherForecast_Success() throws Exception {

        String city = "London";
        WeatherResponse mockResponse = new WeatherResponse();
        mockResponse.setCity(city);
        mockResponse.setCod("200");
        mockResponse.setMessage("Success");

        Mockito.when(weatherService.getWeatherForecast(city)).thenReturn(mockResponse);

        ResponseEntity<WeatherResponse> response = controller.getWeatherForecast(city);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(city, response.getBody().getCity());
        assertEquals("200", response.getBody().getCod());
    }

    @Test
    void testGetWeatherForecast_Unauthorized() throws Exception {
        // Arrange
        String city = "London";
        WeatherResponse mockResponse = new WeatherResponse();
        mockResponse.setCity(city);
        mockResponse.setCod("401");
        mockResponse.setMessage(Message.INVALID_API_KEY);

        Mockito.when(weatherService.getWeatherForecast(city)).thenReturn(mockResponse);

        // Act
        ResponseEntity<WeatherResponse> response = controller.getWeatherForecast(city);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("401", response.getBody().getCod());
        assertEquals(Message.INVALID_API_KEY, response.getBody().getMessage());
    }

    @Test
    void testGetWeatherForecast_CityNotFound() throws Exception {
        // Arrange
        String city = "UnknownCity";
        WeatherResponse mockResponse = new WeatherResponse();
        mockResponse.setCity(city);
        mockResponse.setCod("404");
        mockResponse.setMessage(Message.CITY_NOT_FOUND);

        Mockito.when(weatherService.getWeatherForecast(city)).thenReturn(mockResponse);

        // Act
        ResponseEntity<WeatherResponse> response = controller.getWeatherForecast(city);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("404", response.getBody().getCod());
        assertEquals(Message.CITY_NOT_FOUND, response.getBody().getMessage());
    }

    @Test
    void testGetWeatherForecast_InternalServerError() throws Exception {
        // Arrange
        String city = "London";

        Mockito.when(weatherService.getWeatherForecast(city))
                .thenThrow(new RuntimeException("Some error occurred"));

        // Act
        ResponseEntity<WeatherResponse> response = controller.getWeatherForecast(city);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("500", response.getBody().getCod());
        assertEquals(Message.INTERNAL_SERVER_ERROR, response.getBody().getMessage());
        assertEquals(city, response.getBody().getCity());
    }

    @Test
    void testGetWeatherForecast_EmptyCityParameter() throws Exception {
        // Arrange
        String city = "";
        WeatherResponse mockResponse = new WeatherResponse();
        mockResponse.setCity(city);
        mockResponse.setCod("400");
        mockResponse.setMessage("City parameter is required");

        Mockito.when(weatherService.getWeatherForecast(city)).thenReturn(mockResponse);

        ResponseEntity<WeatherResponse> response = controller.getWeatherForecast(city);

        assertNotNull(response);

    }
}