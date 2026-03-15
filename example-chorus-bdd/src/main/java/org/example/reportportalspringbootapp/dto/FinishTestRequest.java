package org.example.reportportalspringbootapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.reportportalspringbootapp.util.DateTimeUtil;

/**
 * Request for finishing a test item.
 * All fields have sensible defaults for testing and development.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinishTestRequest {

    @NotBlank(message = "Test UUID is required")
    @Builder.Default
    private String testUuid = "default-test-uuid";

    @NotBlank(message = "Launch UUID is required")
    @Builder.Default
    private String launchUuid = "default-launch-uuid";

    @NotBlank(message = "End time is required")
    @Builder.Default
    private String endTime = DateTimeUtil.getCurrentIso8601Timestamp();

    @NotBlank(message = "Project name is required")
    @Builder.Default
    private String projectName = "default-project";
}

