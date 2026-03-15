package org.example.reportportalspringbootapp.dto;

import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request for launch creation with validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaunchRequest {

    @NotBlank(message = "Launch name is required")
    @Size(min = 1, max = 256, message = "Launch name must be between 1 and 256 characters")
    private String launchName;

    @Size(max = 1024, message = "Description cannot exceed 1024 characters")
    private String description;

    private List<ItemAttributesRQ> attributes;

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 256, message = "Project name must be between 1 and 256 characters")
    private String projectName;
}
