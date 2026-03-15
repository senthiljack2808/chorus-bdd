package org.example.reportportalspringbootapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for launch creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaunchResponse {
    
    private String launchId;
    private String projectName;
    private String status;
    
    public LaunchResponse(String launchId, String projectName) {
        this.launchId = launchId;
        this.projectName = projectName;
        this.status = "created";
    }
}
