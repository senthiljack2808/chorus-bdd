package org.example.reportportalspringbootapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.reportportalspringbootapp.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for starting a suite item with validation.
 * All fields have sensible defaults for testing and development.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartSuiteRequest {

    @NotBlank(message = "Launch UUID is required")
    @Builder.Default
    private String launchUuid = "default-launch-uuid";

    @NotBlank(message = "Suite name is required")
    @Size(min = 1, max = 256, message = "Suite name must be between 1 and 256 characters")
    @Builder.Default
    private String name = "Default Suite";

    @Size(max = 1024, message = "Description cannot exceed 1024 characters")
    @Builder.Default
    private String description = "Default suite description";

    @NotBlank(message = "Item type is required")
    @Pattern(regexp = "SUITE|STORY|TEST|SCENARIO|STEP|BEFORE_CLASS|BEFORE_GROUPS|BEFORE_METHOD|BEFORE_SUITE|BEFORE_TEST|AFTER_CLASS|AFTER_GROUPS|AFTER_METHOD|AFTER_SUITE|AFTER_TEST",
            message = "Type must be one of: SUITE, STORY, TEST, SCENARIO, STEP, BEFORE_CLASS, BEFORE_GROUPS, BEFORE_METHOD, BEFORE_SUITE, BEFORE_TEST, AFTER_CLASS, AFTER_GROUPS, AFTER_METHOD, AFTER_SUITE, AFTER_TEST")
    @Builder.Default
    private String type = "SUITE";

    @NotBlank(message = "Start time is required")
    @Builder.Default
    private String startTime = DateTimeUtil.getCurrentIso8601Timestamp();

    @Builder.Default
    private List<ItemAttribute> attributes = new ArrayList<>();

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 256, message = "Project name must be between 1 and 256 characters")
    @Builder.Default
    private String projectName = "default-project";

    /**
     * Inner class for item attributes
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemAttribute {
        @Builder.Default
        private String key = "default-key";
        @Builder.Default
        private String value = "default-value";
    }
}

