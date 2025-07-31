package com.weather.prediction.service.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.weather.prediction.conditions.WeatherCondition;
import com.weather.prediction.constant.Message;
import com.weather.prediction.exception.CustomRuntimeException;
import com.weather.prediction.model.ForecastData;
import com.weather.prediction.model.WeatherResponse;

import com.weather.prediction.service.WeatherService;
import com.weather.prediction.utils.ApiUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WeatherServiceTest {

    private WeatherService weatherService;

    @Mock
    private ApiUtils apiUtils;

    @Mock
    private WeatherCondition weatherCondition;

    private final String testCity = "London";
    private final String apiKey = "test-api-key";
    private final String apiUrl = "http://test.api.url";
    private final String apiCnt = "40";

    @BeforeAll
    void init() {
        MockitoAnnotations.openMocks(this);
        weatherService = new WeatherService(List.of(weatherCondition), apiUtils);
        weatherService.url = apiUrl;
        weatherService.key = apiKey;
        weatherService.cnt = apiCnt;
    }


    @BeforeEach
    void reset(){
        Mockito.reset(apiUtils);
    }

    @Test
    void testGetWeatherForecast_Success() throws Exception {
        // Mock API response
        String mockResponse = """
            {
                "cod": "200",
                "message": 0,
                "list": [
                    {
                        "dt": 1638288000,
                        "main": {
                            "temp": 285.15
                        },
                        "weather": [{"id": 800}]
                    },
                    {
                        "dt": 1638374400,
                        "main": {
                            "temp": 287.15
                        },
                        "weather": [{"id": 801}]
                    }
                ]
            }
            """;

        when(apiUtils.callGetApiWithRetry(anyString())).thenReturn(mockResponse);
        when(weatherCondition.isApplicable(any())).thenReturn(false);

        WeatherResponse response = weatherService.getWeatherForecast(testCity);

        assertNotNull(response);
        assertEquals("200", response.getCod());
        assertEquals(testCity, response.getCity());
        assertNotNull(response.getForecastData());
        assertEquals(1, response.getForecastData().size());
    }

    @Test
    void testGetWeatherForecast_WithAlerts() throws Exception {
        String mockResponse = """
            {
                "cod": "200",
                "message": 0,
                "list": [
                    {
                        "dt": 1638288000,
                        "main": {
                            "temp": 285.15
                        },
                        "weather": [{"id": 800}]
                    },
                    {
                        "dt": 1754125200,
                        "main": {
                            "temp": 285.15
                        },
                        "weather": [{"id": 800}]
                    }
                ]
            }
            """;

        when(apiUtils.callGetApiWithRetry(anyString())).thenReturn(mockResponse);
        when(weatherCondition.isApplicable(any())).thenReturn(true);
        when(weatherCondition.getAlert()).thenReturn("Carry Umbrella");

        WeatherResponse response = weatherService.getWeatherForecast(testCity);

        assertEquals(1, response.getForecastData().get(0).getAlerts().size());
        assertEquals("Carry Umbrella", response.getForecastData().get(0).getAlerts().get(0));
    }

    @Test
    void testGetWeatherForecast_EmptyListResponse() throws Exception {
        String mockResponse = """
            {
                "cod": "200",
                "message": 0,
                "list": []
            }
            """;

        when(apiUtils.callGetApiWithRetry(anyString())).thenReturn(mockResponse);

        WeatherResponse response = weatherService.getWeatherForecast(testCity);

        assertNotNull(response);
        assertEquals("200", response.getCod());
        assertTrue(response.getForecastData().size() == 0);
    }


    @Test
    void testGetWeatherForecast_ResourceAccessException() throws Exception {
        when(apiUtils.callGetApiWithRetry(anyString()))
                .thenThrow(new ResourceAccessException("Service unavailable"));

        WeatherResponse response = weatherService.getWeatherForecast(testCity);

        assertEquals("503", response.getCod());
        assertEquals(Message.SERVICE_UNAVAILABLE, response.getMessage());
    }

}