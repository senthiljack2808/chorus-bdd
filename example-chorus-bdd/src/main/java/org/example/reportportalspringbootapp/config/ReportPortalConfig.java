package org.example.reportportalspringbootapp.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for ReportPortal settings
 */
@Configuration
@Getter
public class ReportPortalConfig {

    @Value("${rp.endpoint}")
    private String endpoint;

    @Value("${rp.api.key}")
    private String apiKey;

    /**
     * Get the base API URL for ReportPortal
     *
     * @return Base API URL
     */
    public String getBaseApiUrl() {
        return endpoint + "/api/v1";
    }
}
