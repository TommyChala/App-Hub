package com.example.app_hub.mappingconfig.dto;

import java.util.List;

public record AssignmentMappingConfigCreateDTO(
        String entityTypeStr,
        String systemId,
        List<AssignmentMappingConfigDTO> data
) {
}
