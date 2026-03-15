package org.example.reportportalspringbootapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.reportportalspringbootapp.util.DateTimeUtil;

/**
 * Request for creating a log entry.
 * All fields have sensible defaults for testing and development.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLogRequest {

    @NotBlank(message = "Launch UUID is required")
    @Builder.Default
    private String launchUuid = "default-launch-uuid";

    @NotBlank(message = "Item UUID is required")
    @Builder.Default
    private String itemUuid = "default-item-uuid";

    @NotBlank(message = "Time is required")
    @Builder.Default
    private String time = DateTimeUtil.getCurrentUnixTimestampMillis();

    @NotBlank(message = "Message is required")
    @Builder.Default
    private String message = "Default log message";

    @NotBlank(message = "Log level is required")
    @Pattern(regexp = "error|warn|info|debug|trace|fatal",
            message = "Level must be one of: error, warn, info, debug, trace, fatal")
    @Builder.Default
    private String level = "info";

    @NotBlank(message = "Project name is required")
    @Builder.Default
    private String projectName = "default-project";
}

