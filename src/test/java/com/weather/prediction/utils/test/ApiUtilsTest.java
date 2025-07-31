package com.weather.prediction.utils.test;

import com.weather.prediction.utils.ApiUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiUtilsTest {

    @Mock
    private RestTemplate restTemplate;

    private ApiUtils apiUtils;

    private final String testUrl = "http://test.api/weather";
    private final String successResponse = "{\"status\":\"success\"}";

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        apiUtils = new ApiUtils(restTemplate);

    }

    @BeforeEach
    void reset() {
        Mockito.reset(restTemplate);
    }

    @Test
    void testCallGetApi_Success() throws RestClientException {
        when(restTemplate.getForObject(eq(testUrl), eq(String.class)))
                .thenReturn(successResponse);

        String result = apiUtils.callGetApi(testUrl);

        assertEquals(successResponse, result);
        verify(restTemplate, times(1)).getForObject(testUrl, String.class);
    }

    @Test
    void testCallGetApi_ThrowsRestClientException() {
        when(restTemplate.getForObject(eq(testUrl), eq(String.class)))
                .thenThrow(new RestClientException("API call failed"));

        assertThrows(RestClientException.class, () -> {
            apiUtils.callGetApi(testUrl);
        });

        verify(restTemplate, times(1)).getForObject(testUrl, String.class);
    }

    @Test
    void testCallGetApiWithRetry_SuccessFirstAttempt() throws RestClientException {
        when(restTemplate.getForObject(eq(testUrl), eq(String.class)))
                .thenReturn(successResponse);

        String result = apiUtils.callGetApiWithRetry(testUrl);

        assertEquals(successResponse, result);
        verify(restTemplate, times(1)).getForObject(testUrl, String.class);
    }
}