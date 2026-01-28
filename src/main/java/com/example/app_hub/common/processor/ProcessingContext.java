package com.example.app_hub.common.processor;

import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;

import java.util.List;
import java.util.Map;

public interface ProcessingContext {

    List<MappingConfigModel> activeMappings();
    Map<String, DataType> targetDataTypes();
    List<String> insertColumns();
}
