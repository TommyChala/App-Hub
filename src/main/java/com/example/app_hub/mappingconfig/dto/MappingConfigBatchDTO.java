package com.example.app_hub.mappingconfig.dto;

import java.util.List;

public record MappingConfigBatchDTO(
        String entityTypeStr,
        String systemId,
        List<MappingConfigModelCreateDTO> data
) {
}
