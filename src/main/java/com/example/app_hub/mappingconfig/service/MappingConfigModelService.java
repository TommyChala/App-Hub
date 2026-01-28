package com.example.app_hub.mappingconfig.service;

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

    public List<MappingConfigModel> createNewAssignmentConfig(AssignmentMappingConfigCreateDTO assignmentMappingConfigCreateDTO) {

        List<MappingConfigModel> addedMappings = new ArrayList<>();
        SystemModel mappingSystem = systemRepository.findBySystemId(Long.valueOf(assignmentMappingConfigCreateDTO.systemId()))
                .orElseThrow(() -> new RuntimeException("Cannot find system")
                );

        for (AssignmentMappingConfigDTO dto : assignmentMappingConfigCreateDTO.data()) {
            MappingConfigModel newMappingConfig = mappingConfigBuilder.buildAssignmentMappingConfig(dto, assignmentMappingConfigCreateDTO.entityTypeStr(), mappingSystem);
            newMappingConfig = mappingConfigModelRepository.save(newMappingConfig);
            if (dto.mappingType().equalsIgnoreCase("Transformation") ||
                    dto.mappingType().equalsIgnoreCase("CONSTANT")) {

                MappingExpressionModel expressionModel = mappingExpressionMapper.toModel(dto.mappingExpression());

                // This method should handle setting the back-reference and saving/adding to list
                mappingConfigBuilder.buildMappingExpression(expressionModel, newMappingConfig);
            }
            addedMappings.add(newMappingConfig);
        }
        return addedMappings;
    }


    public MappingConfigModel addNew(MappingConfigModelCreateDTO dto, String systemId, String entityTypeStr) {

        // 1. Find the system
        SystemModel mappingSystem = systemRepository.findBySystemId(Long.valueOf(systemId))
                .orElseThrow(() -> new RuntimeException("Cannot find system"));

        // 2. Delegate everything to the Builder.
        // Notice: We don't fetch the attribute here anymore!
        // The builder does it using the correct repository based on entityTypeStr.
        MappingConfigModel newMappingConfig = mappingConfigBuilder.buildAttributeMappingConfig(dto, mappingSystem, entityTypeStr, repositoryMapHelper);

        // 3. Save the main config first
        newMappingConfig = mappingConfigModelRepository.save(newMappingConfig);

        // 4. Handle Expressions if needed
        if (dto.mappingType().equalsIgnoreCase("Transformation") ||
                dto.mappingType().equalsIgnoreCase("CONSTANT")) {

            MappingExpressionModel expressionModel = mappingExpressionMapper.toModel(dto.mappingExpression());

            // This method should handle setting the back-reference and saving/adding to list
            mappingConfigBuilder.buildMappingExpression(expressionModel, newMappingConfig);
        }

        return newMappingConfig;
    }
}
