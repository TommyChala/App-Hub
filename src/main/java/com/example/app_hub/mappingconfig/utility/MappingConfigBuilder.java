package com.example.app_hub.mappingconfig.utility;

import com.example.app_hub.assignment.repository.EntitlementAssignmentRepository;
import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.common.repository.BaseEntityAttributeModelRepository;
import com.example.app_hub.common.utility.RepositoryMapHelper;
import com.example.app_hub.mappingconfig.dto.AssignmentMappingConfigDTO;
import com.example.app_hub.mappingconfig.dto.MappingConfigModelCreateDTO;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.mappingconfig.model.MappingExpressionModel;
import com.example.app_hub.mappingconfig.repository.MappingExpressionRepository;
import com.example.app_hub.mappingconfig.type.MappingConfigDataType;
import com.example.app_hub.mappingconfig.type.MappingConfigMappingType;
import com.example.app_hub.system.model.SystemModel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.example.app_hub.common.entitytype.EntityType.ACCOUNT;
import static com.example.app_hub.common.entitytype.EntityType.ENTITLEMENT;

@Component
public class MappingConfigBuilder {

    private final RepositoryMapHelper repositoryMapHelper;
    private final MappingExpressionRepository mappingExpressionRepository;

    public MappingConfigBuilder (RepositoryMapHelper repositoryMapHelper, MappingExpressionRepository mappingExpressionRepository){
        this.repositoryMapHelper = repositoryMapHelper;
        this.mappingExpressionRepository = mappingExpressionRepository;
    }

    public void buildMappingExpression (MappingExpressionModel mappingExpressionModel, MappingConfigModel mappingConfigModel) {
        mappingExpressionModel.setActive(true);
        mappingExpressionModel.setMappingConfig(mappingConfigModel);
        mappingExpressionModel.setCreatedAt(LocalDateTime.now());
        mappingExpressionRepository.save(mappingExpressionModel);
    }


    public MappingConfigModel buildAssignmentMappingConfig (
            AssignmentMappingConfigDTO dto,
            String entityTypeStr,
            SystemModel system
    )
    {
        EntityType entityType = EntityType.valueOf(entityTypeStr.toUpperCase());
        if (entityType != EntityType.ASSIGNMENT) {
            throw new RuntimeException("Incorrect entityType provided.");
        }

        EntitlementAssignmentRepository assignmentRepo = repositoryMapHelper.getEntitlementAssignmentRepository();

        BaseEntityAttributeModelRepository<? extends BaseEntityAttributeModel> attributeRepo = switch (EntityType.valueOf(dto.attribute().type().toUpperCase())) {
            case ACCOUNT -> repositoryMapHelper.getAccountAttributeRepository();
            case ENTITLEMENT -> repositoryMapHelper.getEntitlementAttributeRepository();
            default -> throw new IllegalArgumentException("Unsupported type: " + entityTypeStr);
        };

        BaseEntityAttributeModel attribute = attributeRepo.findByName(dto.attribute().targetAttribute())
                .orElseThrow(() -> new RuntimeException("Attribute not found: " + dto.attribute().targetAttribute())
                );
        MappingConfigModel model = new MappingConfigModel();
        model.setSystem(system);
        model.setEntityType(entityType);
        model.setSourceAttribute(dto.sourceAttribute());

        // THE SENIOR FIX: Just one setter for all attribute types!
        model.setTargetAttribute(attribute);

        model.setDataType(MappingConfigDataType.valueOf(dto.dataType().toUpperCase()));
        model.setMappingType(MappingConfigMappingType.valueOf(dto.mappingType().toUpperCase()));

        return model;

    }


    public MappingConfigModel buildAttributeMappingConfig (
            MappingConfigModelCreateDTO dto,
            SystemModel system,
            String entityTypeStr,
            RepositoryMapHelper repositoryMapHelper
    ) {
        // 1. Resolve the EntityType (ACCOUNT or ENTITLEMENT)
        EntityType entityType = EntityType.valueOf(entityTypeStr.toUpperCase());

        // 2. Resolve the correct repository using our generic interface
        BaseEntityAttributeModelRepository<? extends BaseEntityAttributeModel> repo = switch (entityType) {
            case ACCOUNT -> repositoryMapHelper.getAccountAttributeRepository();
            case ENTITLEMENT -> repositoryMapHelper.getEntitlementAttributeRepository();
            default -> throw new IllegalArgumentException("Unsupported type: " + entityTypeStr);
        };

        // 3. Find the attribute (returns a BaseEntityAttribute)
        BaseEntityAttributeModel attribute = repo.findByName(dto.targetAttribute())
                .orElseThrow(() -> new RuntimeException("Attribute not found: " + dto.targetAttribute()));

        // 4. Build the model
        MappingConfigModel model = new MappingConfigModel();
        model.setSystem(system);
        model.setEntityType(entityType);
        model.setSourceAttribute(dto.sourceAttribute());

        // THE SENIOR FIX: Just one setter for all attribute types!
        model.setTargetAttribute(attribute);

        model.setDataType(MappingConfigDataType.valueOf(dto.dataType().toUpperCase()));
        model.setMappingType(MappingConfigMappingType.valueOf(dto.mappingType().toUpperCase()));

        return model;
    }

}
