package com.weather.prediction.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class ApiUtils {

    private final RestTemplate restTemplate;

    public ApiUtils(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(
            value = {ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String callGetApiWithRetry(String url) throws RestClientException {
        return restTemplate.getForObject(url, String.class);
    }

    public String callGetApi(String url) throws RestClientException{
        return restTemplate.getForObject(url, String.class);
    }







}
