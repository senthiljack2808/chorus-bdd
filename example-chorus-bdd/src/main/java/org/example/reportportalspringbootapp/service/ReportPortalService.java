package org.example.reportportalspringbootapp.service;

import org.example.reportportalspringbootapp.dto.*;

import java.util.Map;

/**
 * Service interface for ReportPortal operations
 */
public interface ReportPortalService {

    /**
     * Creates a new launch in ReportPortal
     * @param request Launch creation request
     * @return Map containing launch ID and project name
     * @throws RuntimeException if launch creation fails
     */
    Map<String, String> createLaunch(LaunchRequest request);

    /**
     * Starts a new suite in ReportPortal
     * @param request Start suite request
     * @return Map containing item ID and project name
     * @throws RuntimeException if starting suite fails
     */
    Map<String, String> startSuite(StartSuiteRequest request);

    /**
     * Starts a new test in ReportPortal under a parent item
     * @param request Start test request
     * @return Map containing item ID and project name
     * @throws RuntimeException if starting test fails
     */
    Map<String, String> startTest(StartTestRequest request);

    /**
     * Starts a new step in ReportPortal under a parent item
     * @param request Start step request
     * @return Map containing item ID and project name
     * @throws RuntimeException if starting step fails
     */
    Map<String, String> startStep(StartStepRequest request);

    /**
     * Finishes a step item with status
     * @param request Finish step request
     * @return Map containing item ID and status
     * @throws RuntimeException if finishing step fails
     */
    Map<String, String> finishStep(FinishStepRequest request);

    /**
     * Creates a log entry for an item
     * @param request Create log request
     * @return Map containing log ID and status
     * @throws RuntimeException if creating log fails
     */
    Map<String, String> addStepLogs(CreateLogRequest request);

    /**
     * Finishes a test item
     * @param request Finish test request
     * @return Map containing item UUID and status
     * @throws RuntimeException if finishing fails
     */
    Map<String, String> finishTest(FinishTestRequest request);

    /**
     * Finishes a suite item
     * @param request Finish suite request
     * @return Map containing item UUID and status
     * @throws RuntimeException if finishing fails
     */
    Map<String, String> finishSuite(FinishSuiteRequest request);

    /**
     * Gets launch information by UUID
     * @param projectName The ReportPortal project name
     * @param launchUuid The UUID of the launch to retrieve
     * @return Map containing the launch information from ReportPortal
     * @throws RuntimeException if launch retrieval fails
     */
    Map<String, Object> getLaunchByUuid(String projectName, String launchUuid);
}
