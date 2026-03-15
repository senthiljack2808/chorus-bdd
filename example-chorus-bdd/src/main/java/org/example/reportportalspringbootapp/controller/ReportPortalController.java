package org.example.reportportalspringbootapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reportportalspringbootapp.dto.*;
import org.example.reportportalspringbootapp.service.ReportPortalService;
import org.example.reportportalspringbootapp.service.TestSuiteService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * REST Controller for ReportPortal operations
 * Follows Spring Boot best practices with proper service layer separation
 */
@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ReportPortalController {

    private final ReportPortalService reportPortalService;
    private final TestSuiteService testSuiteService;

    /**
     * Creates a new launch in ReportPortal
     *
     * @param request Launch creation request with validation
     * @return Standardized API response with launch details
     */
    @PostMapping("/create-launch")
    public ResponseEntity<ApiResponse<LaunchResponse>> createLaunch(
            @Valid @RequestBody LaunchRequest request) {

        log.info("Creating launch '{}' for project '{}'",
                request.getLaunchName(), request.getProjectName());

        Map<String, String> result = reportPortalService.createLaunch(request);

        LaunchResponse responseData = new LaunchResponse(
                result.get("launchId"),
                result.get("projectName")
        );

        log.info("Successfully created launch with ID: {}", result.get("launchId"));

        return ResponseEntity.ok(ApiResponse.success(responseData, "Launch created successfully"));
    }

    /**
     * Starts a new suite in ReportPortal
     *
     * @param request Start suite request with validation
     * @return Standardized API response with suite details
     */
    @PostMapping("/start-suite")
    public ResponseEntity<ApiResponse<ItemOperationResponse>> startSuite(
            @Valid @RequestBody StartSuiteRequest request) {

        log.info("Starting suite '{}' for launch UUID '{}' in project '{}'",
                request.getName(), request.getLaunchUuid(), request.getProjectName());

        Map<String, String> result = reportPortalService.startSuite(request);

        ItemOperationResponse responseData = new ItemOperationResponse(
                result.get("itemUuid"),
                result.get("projectName"),
                "started"
        );

        log.info("Successfully started suite with UUID: {}", result.get("itemUuid"));

        return ResponseEntity.ok(ApiResponse.success(responseData, "Suite started successfully"));
    }

    /**
     * Starts a new test in ReportPortal under a parent item
     *
     * @param request Start test request with validation
     * @return Standardized API response with test details
     */
    @PostMapping("/start-test")
    public ResponseEntity<ApiResponse<ItemOperationResponse>> startTest(
            @Valid @RequestBody StartTestRequest request) {

        log.info("Starting test '{}' under suite UUID '{}' in project '{}'",
                request.getName(), request.getSuiteUuid(), request.getProjectName());

        Map<String, String> result = reportPortalService.startTest(request);

        ItemOperationResponse responseData = new ItemOperationResponse(
                result.get("itemUuid"),
                result.get("projectName"),
                "started"
        );

        log.info("Successfully started test with UUID: {}", result.get("itemUuid"));

        return ResponseEntity.ok(ApiResponse.success(responseData, "Test started successfully"));
    }

    /**
     * Starts a new step in ReportPortal under a parent item
     *
     * @param request Start step request with validation
     * @return Standardized API response with step details
     */
    @PostMapping("/start-step")
    public ResponseEntity<ApiResponse<ItemOperationResponse>> startStep(
            @Valid @RequestBody StartStepRequest request) {

        log.info("Starting step '{}' under test UUID '{}' in project '{}'",
                request.getName(), request.getTestUuid(), request.getProjectName());

        Map<String, String> result = reportPortalService.startStep(request);

        ItemOperationResponse responseData = new ItemOperationResponse(
                result.get("itemUuid"),
                result.get("projectName"),
                "started"
        );

        log.info("Successfully started step with UUID: {}", result.get("itemUuid"));

        return ResponseEntity.ok(ApiResponse.success(responseData, "Step started successfully"));
    }

    /**
     * Finishes a step item with status
     *
     * @param request Finish step request with validation
     * @return Standardized API response with finish details
     */
    @PutMapping("/finish-step")
    public ResponseEntity<ApiResponse<ItemOperationResponse>> finishStep(
            @Valid @RequestBody FinishStepRequest request) {

        log.info("Finishing step UUID '{}' with status '{}' in project '{}'",
                request.getStepUuid(), request.getStatus(), request.getProjectName());

        Map<String, String> result = reportPortalService.finishStep(request);

        ItemOperationResponse responseData = new ItemOperationResponse(
                result.get("itemUuid"),
                request.getProjectName(),
                result.get("status")
        );

        log.info("Successfully finished step UUID: {}", request.getStepUuid());

        return ResponseEntity.ok(ApiResponse.success(responseData, "Step finished successfully"));
    }

    /**
     * Creates a log entry for an item
     *
     * @param request Create log request with validation
     * @return Standardized API response with log details
     */
    @PostMapping("/add-step-logs")
    public ResponseEntity<ApiResponse<ItemOperationResponse>> addStepLogs(
            @Valid @RequestBody CreateLogRequest request) {

        log.info("Creating log entry for item UUID '{}' in project '{}'",
                request.getItemUuid(), request.getProjectName());

        Map<String, String> result = reportPortalService.addStepLogs(request);

        ItemOperationResponse responseData = new ItemOperationResponse(
                result.get("itemUuid"),
                request.getProjectName(),
                result.get("status")
        );

        log.info("Successfully created log entry for item UUID: {}", request.getItemUuid());

        return ResponseEntity.ok(ApiResponse.success(responseData, "Log created successfully"));
    }

    /**
     * Finishes a test item
     *
     * @param request Finish test request with validation
     * @return Standardized API response with finish details
     */
    @PutMapping("/finish-test")
    public ResponseEntity<ApiResponse<ItemOperationResponse>> finishTest(
            @Valid @RequestBody FinishTestRequest request) {

        log.info("Finishing test UUID '{}' in project '{}'",
                request.getTestUuid(), request.getProjectName());

        Map<String, String> result = reportPortalService.finishTest(request);

        ItemOperationResponse responseData = new ItemOperationResponse(
                result.get("itemUuid"),
                request.getProjectName(),
                result.get("status")
        );

        log.info("Successfully finished test UUID: {}", request.getTestUuid());

        return ResponseEntity.ok(ApiResponse.success(responseData, "Test finished successfully"));
    }

    /**
     * Finishes a suite item
     *
     * @param request Finish suite request with validation
     * @return Standardized API response with finish details
     */
    @PutMapping("/finish-suite")
    public ResponseEntity<ApiResponse<ItemOperationResponse>> finishSuite(
            @Valid @RequestBody FinishSuiteRequest request) {

        log.info("Finishing suite UUID '{}' in project '{}'",
                request.getSuiteUuid(), request.getProjectName());

        Map<String, String> result = reportPortalService.finishSuite(request);

        ItemOperationResponse responseData = new ItemOperationResponse(
                result.get("itemUuid"),
                request.getProjectName(),
                result.get("status")
        );

        log.info("Successfully finished suite UUID: {}", request.getSuiteUuid());

        return ResponseEntity.ok(ApiResponse.success(responseData, "Suite finished successfully"));
    }

    /**
     * Gets the owner of a test suite
     *
     * @param suiteName The name of the test suite
     * @return Standardized API response with owner information
     */
    @GetMapping("/suite-owner")
    public ResponseEntity<ApiResponse<OwnerResponse>> getSuiteOwner(
            @RequestParam @NotBlank(message = "Suite name is required") String suiteName) {

        log.info("Fetching owner for test suite: {}", suiteName);

        String ownerName = testSuiteService.getOwnerName(suiteName);

        if (ownerName == null) {
            log.warn("No owner found for test suite: {}", suiteName);
            return ResponseEntity.ok(ApiResponse.success(
                    new OwnerResponse(null, suiteName),
                    "No owner found for the specified test suite"
            ));
        }

        OwnerResponse responseData = new OwnerResponse(ownerName, suiteName);

        log.info("Found owner '{}' for test suite '{}'", ownerName, suiteName);

        return ResponseEntity.ok(ApiResponse.success(responseData, "Owner retrieved successfully"));
    }

    /**
     * Gets launch information by UUID from ReportPortal
     *
     * @param projectName The ReportPortal project name
     * @param launchUuid The UUID of the launch to retrieve
     * @return Standardized API response with launch information
     */
    @GetMapping("/{projectName}/launch/uuid/{launchUuid}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLaunchByUuid(
            @PathVariable @NotBlank(message = "Project name is required") String projectName,
            @PathVariable @NotBlank(message = "Launch UUID is required") String launchUuid) {

        log.info("Fetching launch with UUID '{}' from project '{}'", launchUuid, projectName);

        Map<String, Object> launchData = reportPortalService.getLaunchByUuid(projectName, launchUuid);

        log.info("Successfully retrieved launch with UUID '{}' from project '{}'", launchUuid, projectName);

        return ResponseEntity.ok(ApiResponse.success(launchData, "Launch retrieved successfully"));
    }
}
