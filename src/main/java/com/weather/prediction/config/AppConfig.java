package com.weather.prediction.config;

import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;



@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(){


        return new RestTemplate();
    }
}
