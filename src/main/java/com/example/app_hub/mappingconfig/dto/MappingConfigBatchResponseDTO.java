package com.example.app_hub.mappingconfig.dto;

import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;

import java.util.List;

public record MappingConfigBatchResponseDTO(
        List<BaseEntityAttributeModel> mappingConfigs
) {
}
