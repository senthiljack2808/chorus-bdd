package org.example.reportportalspringbootapp.service.impl;

import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import java.util.LinkedHashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reportportalspringbootapp.config.ReportPortalConfig;
import org.example.reportportalspringbootapp.dto.*;
import org.example.reportportalspringbootapp.exception.ReportPortalServiceException;
import org.example.reportportalspringbootapp.service.ReportPortalService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Implementation of ReportPortal service operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportPortalServiceImpl implements ReportPortalService {

    // ReportPortal configuration constants
    private static final String RP_PROJECT_PROPERTY = "rp.project";

    private final ReportPortalConfig reportPortalConfig;
    private final RestTemplate restTemplate;

    @Override
    public Map<String, String> createLaunch(LaunchRequest request) {
        validateLaunchRequest(request);
        
        try {
            ReportPortal reportPortal = createReportPortalWithProject(request.getProjectName());
            
            // Create initial launch
            Launch launch = createInitialLaunch(reportPortal, request);
            String launchId = launch.start().blockingGet();
            
            // Finish initial launch
            finishLaunch(launch);
            
            // Create rerun launch
            Launch rerunLaunch = createRerunLaunch(reportPortal, request, launchId);
            String rerunUUID = rerunLaunch.start().blockingGet();
            
            // Finish rerun launch
            finishLaunch(rerunLaunch);
            
            log.info("Successfully created rerun launch with UUID: {}", rerunUUID);
            
            Map<String, String> response = new HashMap<>();
            response.put("launchId", rerunUUID);
            response.put("projectName", request.getProjectName());
            return response;
            
        } catch (Exception e) {
            log.error("Failed to create launch for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to create launch: " + e.getMessage(), e);
        }
    }

    private void validateLaunchRequest(LaunchRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (request.getLaunchName() == null || request.getLaunchName().trim().isEmpty()) {
            throw new IllegalArgumentException("Launch name is required");
        }
    }

    private synchronized ReportPortal createReportPortalWithProject(String projectName) {
        try {
            Properties baseProperties = loadReportPortalProperties();
            Map<String, String> originalProperties = backupSystemProperties(baseProperties);
            
            setSystemProperties(baseProperties, projectName);
            ReportPortal reportPortal = ReportPortal.builder().build();
            restoreSystemProperties(originalProperties);
            
            return reportPortal;
            
        } catch (IOException e) {
            throw new ReportPortalServiceException("Failed to load ReportPortal properties", e);
        }
    }

    private Properties loadReportPortalProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("reportportal.properties")) {
            if (input != null) {
                properties.load(input);
            }
        }
        return properties;
    }

    private Map<String, String> backupSystemProperties(Properties properties) {
        Map<String, String> backup = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            backup.put(key, System.getProperty(key));
        }
        backup.put(RP_PROJECT_PROPERTY, System.getProperty(RP_PROJECT_PROPERTY));
        return backup;
    }

    private void setSystemProperties(Properties properties, String projectName) {
        for (String key : properties.stringPropertyNames()) {
            System.setProperty(key, properties.getProperty(key));
        }
        System.setProperty(RP_PROJECT_PROPERTY, projectName);
    }

    private void restoreSystemProperties(Map<String, String> originalProperties) {
        for (Map.Entry<String, String> entry : originalProperties.entrySet()) {
            if (entry.getValue() != null) {
                System.setProperty(entry.getKey(), entry.getValue());
            } else {
                System.clearProperty(entry.getKey());
            }
        }
    }

    private Launch createInitialLaunch(ReportPortal reportPortal, LaunchRequest request) {
        StartLaunchRQ rq = new StartLaunchRQ();
        rq.setName(request.getLaunchName());
        rq.setStartTime(new Date());
        rq.setMode(com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT);
        
        if (request.getDescription() != null) {
            rq.setDescription(request.getDescription());
        }
        
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            rq.setAttributes(new LinkedHashSet<>(request.getAttributes()));
        }
        
        return reportPortal.newLaunch(rq);
    }

    private Launch createRerunLaunch(ReportPortal reportPortal, LaunchRequest request, String originalLaunchId) {
        StartLaunchRQ rerunRQ = new StartLaunchRQ();
        rerunRQ.setName(request.getLaunchName());
        rerunRQ.setStartTime(new Date());
        rerunRQ.setMode(com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT);
        rerunRQ.setDescription(request.getDescription());
        
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            rerunRQ.setAttributes(new LinkedHashSet<>(request.getAttributes()));
        }
        
        rerunRQ.setRerun(true);
        rerunRQ.setRerunOf(originalLaunchId);
        
        return reportPortal.newLaunch(rerunRQ);
    }

    private void finishLaunch(Launch launch) {
        FinishExecutionRQ finishRQ = new FinishExecutionRQ();
        finishRQ.setEndTime(new Date());
        launch.finish(finishRQ);
    }

    @Override
    public Map<String, String> startSuite(StartSuiteRequest request) {
        validateStartSuiteRequest(request);

        try {
            // Build the API URL
            String url = reportPortalConfig.getBaseApiUrl() + "/" + request.getProjectName() + "/item";

            log.info("Starting suite '{}' for launch UUID '{}' in project '{}'",
                    request.getName(), request.getLaunchUuid(), request.getProjectName());

            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");
            headers.set("Authorization", "Bearer " + reportPortalConfig.getApiKey());

            // Prepare request body
            Map<String, Object> requestBody = buildStartSuiteRequestBody(request);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Extract item ID from response
            String itemUuid = extractItemId(response);

            log.info("Successfully started suite with UUID: {}", itemUuid);

            Map<String, String> result = new HashMap<>();
            result.put("itemUuid", itemUuid);
            result.put("projectName", request.getProjectName());
            return result;

        } catch (RestClientException e) {
            log.error("Failed to start suite for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to start suite: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while starting suite for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to start suite: " + e.getMessage(), e);
        }
    }

    private void validateStartSuiteRequest(StartSuiteRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (request.getLaunchUuid() == null || request.getLaunchUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Launch UUID is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Suite name is required");
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Item type is required");
        }
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            throw new IllegalArgumentException("Start time is required");
        }
    }

    private Map<String, Object> buildStartSuiteRequestBody(StartSuiteRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("launchUuid", request.getLaunchUuid());
        body.put("name", request.getName());
        body.put("type", request.getType());
        body.put("startTime", request.getStartTime());

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            body.put("description", request.getDescription());
        }

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            List<Map<String, String>> attributes = request.getAttributes().stream()
                    .map(attr -> {
                        Map<String, String> attrMap = new HashMap<>();
                        attrMap.put("key", attr.getKey());
                        attrMap.put("value", attr.getValue());
                        return attrMap;
                    })
                    .collect(Collectors.toList());
            body.put("attributes", attributes);
        }

        return body;
    }

    private String extractItemId(ResponseEntity<Map> response) {
        if (response.getBody() == null) {
            throw new ReportPortalServiceException("Empty response from ReportPortal API", null);
        }

        Map<String, Object> responseBody = response.getBody();

        // Try to extract ID from common response fields
        if (responseBody.containsKey("id")) {
            return String.valueOf(responseBody.get("id"));
        } else if (responseBody.containsKey("uuid")) {
            return String.valueOf(responseBody.get("uuid"));
        } else if (responseBody.containsKey("itemId")) {
            return String.valueOf(responseBody.get("itemId"));
        }

        // If no standard field found, log the response and throw exception
        log.error("Unable to extract item ID from response: {}", responseBody);
        throw new ReportPortalServiceException("Unable to extract item ID from API response", null);
    }

    @Override
    public Map<String, String> startTest(StartTestRequest request) {
        validateStartTestRequest(request);

        try {
            // Build the API URL with suite UUID
            String url = reportPortalConfig.getBaseApiUrl() + "/" + request.getProjectName() + "/item/" + request.getSuiteUuid();

            log.info("Starting test '{}' under suite UUID '{}' in project '{}'",
                    request.getName(), request.getSuiteUuid(), request.getProjectName());

            // Prepare request headers
            HttpHeaders headers = createHeaders();

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", request.getName());
            requestBody.put("startTime", request.getStartTime());
            requestBody.put("type", "test");
            requestBody.put("launchUuid", request.getLaunchUuid());

            if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
                requestBody.put("description", request.getDescription());
            }

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            // Extract item ID from response
            String itemUuid = extractItemId(response);

            log.info("Successfully started test with UUID: {}", itemUuid);

            Map<String, String> result = new HashMap<>();
            result.put("itemUuid", itemUuid);
            result.put("projectName", request.getProjectName());
            return result;

        } catch (RestClientException e) {
            log.error("Failed to start test for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to start test: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while starting test for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to start test: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> startStep(StartStepRequest request) {
        validateStartStepRequest(request);

        try {
            // Build the API URL with test UUID
            String url = reportPortalConfig.getBaseApiUrl() + "/" + request.getProjectName() + "/item/" + request.getTestUuid();

            log.info("Starting step '{}' under test UUID '{}' in project '{}'",
                    request.getName(), request.getTestUuid(), request.getProjectName());

            // Prepare request headers
            HttpHeaders headers = createHeaders();

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", request.getName());
            requestBody.put("startTime", request.getStartTime());
            requestBody.put("type", "step");
            requestBody.put("launchUuid", request.getLaunchUuid());

            if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
                requestBody.put("description", request.getDescription());
            }

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            // Extract item ID from response
            String itemUuid = extractItemId(response);

            log.info("Successfully started step with UUID: {}", itemUuid);

            Map<String, String> result = new HashMap<>();
            result.put("itemUuid", itemUuid);
            result.put("projectName", request.getProjectName());
            return result;

        } catch (RestClientException e) {
            log.error("Failed to start step for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to start step: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while starting step for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to start step: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> finishStep(FinishStepRequest request) {
        validateFinishStepRequest(request);

        try {
            // Build the API URL with step UUID
            String url = reportPortalConfig.getBaseApiUrl() + "/" + request.getProjectName() + "/item/" + request.getStepUuid();

            log.info("Finishing step UUID '{}' with status '{}' in project '{}'",
                    request.getStepUuid(), request.getStatus(), request.getProjectName());

            // Prepare request headers
            HttpHeaders headers = createHeaders();

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("endTime", request.getEndTime());
            requestBody.put("status", request.getStatus());
            requestBody.put("launchUuid", request.getLaunchUuid());

            if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
                List<Map<String, Object>> attributes = request.getAttributes().stream()
                        .map(attr -> {
                            Map<String, Object> attrMap = new HashMap<>();
                            attrMap.put("key", attr.getKey());
                            attrMap.put("value", attr.getValue());
                            if (attr.getSystem() != null) {
                                attrMap.put("system", attr.getSystem());
                            }
                            return attrMap;
                        })
                        .collect(Collectors.toList());
                requestBody.put("attributes", attributes);
            }

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call (PUT method)
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);

            log.info("Successfully finished step UUID: {}", request.getStepUuid());

            Map<String, String> result = new HashMap<>();
            result.put("itemUuid", request.getStepUuid());
            result.put("status", request.getStatus());
            return result;

        } catch (RestClientException e) {
            log.error("Failed to finish step for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to finish step: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while finishing step for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to finish step: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> addStepLogs(CreateLogRequest request) {
        validateCreateLogRequest(request);

        try {
            // Build the API URL for logs
            String url = reportPortalConfig.getBaseApiUrl() + "/" + request.getProjectName() + "/log";

            log.info("Creating log entry for item UUID '{}' in project '{}'",
                    request.getItemUuid(), request.getProjectName());

            // Prepare request headers
            HttpHeaders headers = createHeaders();

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("launchUuid", request.getLaunchUuid());
            requestBody.put("itemUuid", request.getItemUuid());
            requestBody.put("time", request.getTime());
            requestBody.put("message", request.getMessage());
            requestBody.put("level", request.getLevel());

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("Successfully created log entry for item UUID: {}", request.getItemUuid());

            Map<String, String> result = new HashMap<>();
            result.put("itemUuid", request.getItemUuid());
            result.put("status", "log_created");
            return result;

        } catch (RestClientException e) {
            log.error("Failed to create log for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to create log: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while creating log for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to create log: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> finishTest(FinishTestRequest request) {
        validateFinishTestRequest(request);

        try {
            // Build the API URL with test UUID
            String url = reportPortalConfig.getBaseApiUrl() + "/" + request.getProjectName() + "/item/" + request.getTestUuid();

            log.info("Finishing test UUID '{}' in project '{}'",
                    request.getTestUuid(), request.getProjectName());

            // Prepare request headers
            HttpHeaders headers = createHeaders();

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("endTime", request.getEndTime());
            requestBody.put("launchUuid", request.getLaunchUuid());

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call (PUT method)
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);

            log.info("Successfully finished test UUID: {}", request.getTestUuid());

            Map<String, String> result = new HashMap<>();
            result.put("itemUuid", request.getTestUuid());
            result.put("status", "finished");
            return result;

        } catch (RestClientException e) {
            log.error("Failed to finish test for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to finish test: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while finishing test for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to finish test: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> finishSuite(FinishSuiteRequest request) {
        validateFinishSuiteRequest(request);

        try {
            // Build the API URL with suite UUID
            String url = reportPortalConfig.getBaseApiUrl() + "/" + request.getProjectName() + "/item/" + request.getSuiteUuid();

            log.info("Finishing suite UUID '{}' in project '{}'",
                    request.getSuiteUuid(), request.getProjectName());

            // Prepare request headers
            HttpHeaders headers = createHeaders();

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("endTime", request.getEndTime());
            requestBody.put("launchUuid", request.getLaunchUuid());

            // Create HTTP entity
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the API call (PUT method)
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);

            log.info("Successfully finished suite UUID: {}", request.getSuiteUuid());

            Map<String, String> result = new HashMap<>();
            result.put("itemUuid", request.getSuiteUuid());
            result.put("status", "finished");
            return result;

        } catch (RestClientException e) {
            log.error("Failed to finish suite for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to finish suite: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while finishing suite for project: {}", request.getProjectName(), e);
            throw new ReportPortalServiceException("Failed to finish suite: " + e.getMessage(), e);
        }
    }

    // Helper method to create headers
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + reportPortalConfig.getApiKey());
        return headers;
    }

    // Validation methods
    private void validateStartTestRequest(StartTestRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (request.getSuiteUuid() == null || request.getSuiteUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Suite UUID is required");
        }
        if (request.getLaunchUuid() == null || request.getLaunchUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Launch UUID is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Test name is required");
        }
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            throw new IllegalArgumentException("Start time is required");
        }
    }

    private void validateStartStepRequest(StartStepRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (request.getTestUuid() == null || request.getTestUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Test UUID is required");
        }
        if (request.getLaunchUuid() == null || request.getLaunchUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Launch UUID is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Step name is required");
        }
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            throw new IllegalArgumentException("Start time is required");
        }
    }

    private void validateFinishStepRequest(FinishStepRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (request.getStepUuid() == null || request.getStepUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Step UUID is required");
        }
        if (request.getLaunchUuid() == null || request.getLaunchUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Launch UUID is required");
        }
        if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
            throw new IllegalArgumentException("End time is required");
        }
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
    }

    private void validateCreateLogRequest(CreateLogRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (request.getLaunchUuid() == null || request.getLaunchUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Launch UUID is required");
        }
        if (request.getItemUuid() == null || request.getItemUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Item UUID is required");
        }
        if (request.getTime() == null || request.getTime().trim().isEmpty()) {
            throw new IllegalArgumentException("Time is required");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Message is required");
        }
        if (request.getLevel() == null || request.getLevel().trim().isEmpty()) {
            throw new IllegalArgumentException("Log level is required");
        }
    }

    private void validateFinishTestRequest(FinishTestRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (request.getTestUuid() == null || request.getTestUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Test UUID is required");
        }
        if (request.getLaunchUuid() == null || request.getLaunchUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Launch UUID is required");
        }
        if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
            throw new IllegalArgumentException("End time is required");
        }
    }

    private void validateFinishSuiteRequest(FinishSuiteRequest request) {
        if (request.getProjectName() == null || request.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (request.getSuiteUuid() == null || request.getSuiteUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Suite UUID is required");
        }
        if (request.getLaunchUuid() == null || request.getLaunchUuid().trim().isEmpty()) {
            throw new IllegalArgumentException("Launch UUID is required");
        }
        if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
            throw new IllegalArgumentException("End time is required");
        }
    }

    @Override
    public Map<String, Object> getLaunchByUuid(String projectName, String launchUuid) {
        validateGetLaunchRequest(projectName, launchUuid);

        try {
            // Build the API URL for launch by UUID
            String url = reportPortalConfig.getBaseApiUrl() + "/" + projectName + "/launch/uuid/" + launchUuid;

            log.info("Fetching launch with UUID '{}' from project '{}'", launchUuid, projectName);

            // Prepare request headers
            HttpHeaders headers = createHeaders();

            // Create HTTP entity with headers only (GET request, no body)
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Make the API call
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() == null) {
                throw new ReportPortalServiceException("Empty response from ReportPortal API", null);
            }

            log.info("Successfully retrieved launch with UUID: {}", launchUuid);

            return response.getBody();

        } catch (RestClientException e) {
            log.error("Failed to fetch launch UUID '{}' from project '{}': {}", launchUuid, projectName, e.getMessage());
            throw new ReportPortalServiceException("Failed to retrieve launch: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching launch UUID '{}' from project '{}': {}", launchUuid, projectName, e.getMessage());
            throw new ReportPortalServiceException("Failed to retrieve launch: " + e.getMessage(), e);
        }
    }

    private void validateGetLaunchRequest(String projectName, String launchUuid) {
        if (projectName == null || projectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (launchUuid == null || launchUuid.trim().isEmpty()) {
            throw new IllegalArgumentException("Launch UUID is required");
        }
    }
}
