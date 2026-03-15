package org.example.reportportalspringbootapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.reportportalspringbootapp.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for finishing a step item with status.
 * All fields have sensible defaults for testing and development.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinishStepRequest {

    @NotBlank(message = "Step UUID is required")
    @Builder.Default
    private String stepUuid = "default-step-uuid";

    @NotBlank(message = "Launch UUID is required")
    @Builder.Default
    private String launchUuid = "default-launch-uuid";

    @NotBlank(message = "End time is required")
    @Builder.Default
    private String endTime = DateTimeUtil.getCurrentIso8601Timestamp();

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "passed|failed|stopped|skipped|interrupted|cancelled",
            message = "Status must be one of: passed, failed, stopped, skipped, interrupted, cancelled")
    @Builder.Default
    private String status = "passed";

    @Builder.Default
    private List<StepAttribute> attributes = new ArrayList<>();

    @NotBlank(message = "Project name is required")
    @Builder.Default
    private String projectName = "default-project";

    /**
     * Inner class for step attributes
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepAttribute {
        @Builder.Default
        private String key = "default-key";
        @Builder.Default
        private String value = "default-value";
        @Builder.Default
        private Boolean system = false;
    }
}

