package com.example.app_hub.importing.dto;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.importing.type.ImportType;

public record CollectorRawCreateRequest(
        String systemId,
        ImportType collectorType,
        EntityType entityType,
        String filePath
) {
}
