package com.example.app_hub.account.processor;

import com.example.app_hub.account.model.AccountAttributeModel;
import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.common.processor.ProcessingContext;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record AccountProcessingContext(
        List<MappingConfigModel> activeMappings,
        Map<String, DataType> targetDataTypes,
        List<String> insertColumns,
        Set<String> usedAttributes,
        List<AccountAttributeModel> allAttributes
) implements ProcessingContext {}
