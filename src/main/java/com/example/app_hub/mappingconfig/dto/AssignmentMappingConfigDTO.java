package com.example.app_hub.mappingconfig.dto;

public record AssignmentMappingConfigDTO(
        AssignmentMappingConfigAttribute attribute,
        String sourceAttribute,
        String mappingType,
        String dataType,
        MappingExpressionCreateDTO mappingExpression
) {
}
