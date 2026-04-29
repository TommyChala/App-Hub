package com.example.app_hub.mappingconfig.service;

import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.common.utility.RepositoryMapHelper;
import com.example.app_hub.mappingconfig.dto.AssignmentMappingConfigCreateDTO;
import com.example.app_hub.mappingconfig.dto.AssignmentMappingConfigDTO;
import com.example.app_hub.mappingconfig.dto.MappingConfigModelCreateDTO;
import com.example.app_hub.mappingconfig.mapper.MappingExpressionMapper;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.mappingconfig.model.MappingExpressionModel;
import com.example.app_hub.mappingconfig.repository.MappingConfigModelRepository;
import com.example.app_hub.mappingconfig.utility.MappingConfigBuilder;
import com.example.app_hub.system.model.SystemModel;
import com.example.app_hub.system.repository.SystemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MappingConfigModelService {
    private final MappingConfigModelRepository mappingConfigModelRepository;
    private final SystemRepository systemRepository;
    private final MappingExpressionMapper mappingExpressionMapper;
    private final MappingConfigBuilder mappingConfigBuilder;
    private final RepositoryMapHelper repositoryMapHelper;

    public MappingConfigModelService(
            MappingConfigModelRepository mappingConfigModelRepository,
            SystemRepository systemRepository,
            MappingExpressionMapper mappingExpressionMapper,
            MappingConfigBuilder mappingConfigBuilder,
            RepositoryMapHelper repositoryMapHelper
    ) {
        this.mappingConfigModelRepository = mappingConfigModelRepository;
        this.systemRepository = systemRepository;
        this.mappingExpressionMapper = mappingExpressionMapper;
        this.mappingConfigBuilder = mappingConfigBuilder;
        this.repositoryMapHelper = repositoryMapHelper;
    }

    public List<BaseEntityAttributeModel> createNewAssignmentConfig(AssignmentMappingConfigCreateDTO assignmentMappingConfigCreateDTO) {

        List<BaseEntityAttributeModel> addedMappings = new ArrayList<>();
        SystemModel mappingSystem = systemRepository.findBySystemId(Long.valueOf(assignmentMappingConfigCreateDTO.systemId()))
                .orElseThrow(() -> new RuntimeException("Cannot find system")
                );

        for (AssignmentMappingConfigDTO dto : assignmentMappingConfigCreateDTO.data()) {
            MappingConfigModel newMappingConfig = mappingConfigBuilder.buildAssignmentMappingConfig(dto, assignmentMappingConfigCreateDTO.entityTypeStr(), mappingSystem);
            newMappingConfig = mappingConfigModelRepository.save(newMappingConfig);
            if (dto.mappingType().equalsIgnoreCase("Transformation") ||
                    dto.mappingType().equalsIgnoreCase("CONSTANT")) {

                MappingExpressionModel expressionModel = mappingExpressionMapper.toModel(dto.mappingExpression());

                mappingConfigBuilder.buildMappingExpression(expressionModel, newMappingConfig);
            }
            addedMappings.add(newMappingConfig.getTargetAttribute());
        }
        return addedMappings;
    }

    public BaseEntityAttributeModel addNew(MappingConfigModelCreateDTO dto, String systemId, String entityTypeStr) {


        SystemModel mappingSystem = systemRepository.findBySystemId(Long.valueOf(systemId))
                .orElseThrow(() -> new RuntimeException("Cannot find system"));

        MappingConfigModel newMappingConfig = mappingConfigBuilder.buildAttributeMappingConfig(dto, mappingSystem, entityTypeStr, repositoryMapHelper);

        newMappingConfig = mappingConfigModelRepository.save(newMappingConfig);


        if (dto.mappingType().equalsIgnoreCase("Transformation") ||
                dto.mappingType().equalsIgnoreCase("CONSTANT")) {

            MappingExpressionModel expressionModel = mappingExpressionMapper.toModel(dto.mappingExpression());

            // This method should handle setting the back-reference and saving/adding to list
            mappingConfigBuilder.buildMappingExpression(expressionModel, newMappingConfig);
        }

        return newMappingConfig.getTargetAttribute();
    }
}
