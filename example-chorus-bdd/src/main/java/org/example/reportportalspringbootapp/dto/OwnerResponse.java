package org.example.reportportalspringbootapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for suite owner information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerResponse {
    
    private String ownerName;
    private String suiteName;
}
