package org.example.reportportalspringbootapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response for item operations (create, update, finish)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemOperationResponse {

    private String itemUuid;
    private String projectName;
    private String status;
    private String message;

    public ItemOperationResponse(String itemUuid, String projectName, String status) {
        this.itemUuid = itemUuid;
        this.projectName = projectName;
        this.status = status;
    }
}

