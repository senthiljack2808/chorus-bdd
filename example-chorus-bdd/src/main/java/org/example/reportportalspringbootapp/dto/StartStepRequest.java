package org.example.reportportalspringbootapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.reportportalspringbootapp.util.DateTimeUtil;

/**
 * Request for starting a step item under a test.
 * All fields have sensible defaults for testing and development.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartStepRequest {

    @NotBlank(message = "Test UUID is required")
    @Builder.Default
    private String testUuid = "default-test-uuid";

    @NotBlank(message = "Launch UUID is required")
    @Builder.Default
    private String launchUuid = "default-launch-uuid";

    @NotBlank(message = "Step name is required")
    @Size(min = 1, max = 256, message = "Step name must be between 1 and 256 characters")
    @Builder.Default
    private String name = "Default Step";

    @Size(max = 1024, message = "Description cannot exceed 1024 characters")
    @Builder.Default
    private String description = "Default step description";

    @NotBlank(message = "Start time is required")
    @Builder.Default
    private String startTime = DateTimeUtil.getCurrentIso8601Timestamp();

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 256, message = "Project name must be between 1 and 256 characters")
    @Builder.Default
    private String projectName = "default-project";
}

