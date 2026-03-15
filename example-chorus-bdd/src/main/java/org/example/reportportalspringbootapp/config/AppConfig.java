package org.example.reportportalspringbootapp.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Application configuration class for Spring Boot beans
 * Updated for Spring Boot 3.x compatibility
 */
@Configuration
public class AppConfig {

    /**
     * Creates a RestTemplate bean with proper configuration using modern Spring Boot 3.x approach
     *
     * @param builder RestTemplateBuilder provided by Spring Boot
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }

    /**
     * Creates a ClientHttpRequestFactory with timeout configuration
     * This is the modern way to configure timeouts in Spring Boot 3.x
     *
     * @return Configured ClientHttpRequestFactory
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));
        factory.setReadTimeout(Duration.ofSeconds(30));
        return factory;
    }
}
