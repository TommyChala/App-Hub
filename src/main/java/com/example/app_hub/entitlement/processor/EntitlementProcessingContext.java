package com.example.app_hub.entitlement.processor;

import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.common.processor.ProcessingContext;
import com.example.app_hub.entitlement.model.EntitlementAttributeModel;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record EntitlementProcessingContext(
        List<MappingConfigModel> activeMappings,
        Map<String, DataType> targetDataTypes,
        List<String> insertColumns,
        Set<String> usedAttributeNames,
        List<EntitlementAttributeModel> allAttributes
) implements ProcessingContext {}
