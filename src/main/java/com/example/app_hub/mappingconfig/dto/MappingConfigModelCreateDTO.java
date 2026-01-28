package com.example.app_hub.mappingconfig.dto;

public record MappingConfigModelCreateDTO(
        String sourceAttribute,
        String targetAttribute,
        String dataType,
        String mappingType,
        MappingExpressionCreateDTO mappingExpression
) {
}
