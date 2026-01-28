package com.example.app_hub.assignment.processor;

import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.common.processor.ProcessingContext;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record EntitlementAssignmentProcessingContext(
        List<MappingConfigModel> activeMappings,
        Map<String, DataType> targetDataTypes,
        List<String> insertColumns,
        Set<String> usedAttributes
) implements ProcessingContext {}
