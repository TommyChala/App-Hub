package com.example.app_hub.mappingconfig.utility;

import com.example.app_hub.account.repository.AccountAttributeRepository;
import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.common.repository.BaseEntityAttributeModelRepository;
import com.example.app_hub.common.utility.RepositoryMapHelper;
import com.example.app_hub.mappingconfig.dto.MappingConfigModelCreateDTO;
import com.example.app_hub.system.model.SystemModel;
import com.example.app_hub.system.repository.SystemRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MappingConfigValidator {

    private final SystemRepository systemRepository;
    private final RepositoryMapHelper repositoryMapHelper;

    public MappingConfigValidator(
            SystemRepository systemRepository,
            RepositoryMapHelper repositoryMapHelper
    ) {
        this.systemRepository = systemRepository;
        this.repositoryMapHelper = repositoryMapHelper;
    }

    public void validateMandatoryAttributes(
            List<MappingConfigModelCreateDTO> mappings,
            String systemId,
            String entityTypeStr
    ) {
        Set<String> invalidMappings = mappings.stream()
                .filter(m -> m.mappingType() == null || (
                        // Case 1: If it's a TRANSFORMATION, it MUST have an expression
                        (m.mappingType().equalsIgnoreCase("TRANSFORMATION") &&
                                (m.mappingExpression() == null || m.mappingExpression().expression() == null || m.mappingExpression().expression().isBlank()))
                                ||
                                // Case 2: If it's a CONSTANT, it also MUST have an expression (if you're reusing that field)
                                (m.mappingType().equalsIgnoreCase("CONSTANT") &&
                                        (m.mappingExpression() == null || m.mappingExpression().expression() == null || m.mappingExpression().expression().isBlank()))
                ))
                .map(MappingConfigModelCreateDTO::targetAttribute)
                .collect(Collectors.toSet());

        if (!invalidMappings.isEmpty()) {
            throw new IllegalArgumentException("Incomplete mapping configuration for: " + invalidMappings +
                    ". Ensure all have a mappingType, and TRANSFORMATIONS have an expression.");
        }

        SystemModel system = systemRepository.findBySystemId(Long.valueOf(systemId))
                .orElseThrow(() -> new RuntimeException("System not found")
                );
        // 1. Get all attributes for this system (or system-agnostic)
        BaseEntityAttributeModelRepository<? extends BaseEntityAttributeModel> repo = switch (EntityType.valueOf(entityTypeStr.toUpperCase())) {
            case ACCOUNT -> repositoryMapHelper.getAccountAttributeRepository();
            case ENTITLEMENT -> repositoryMapHelper.getEntitlementAttributeRepository();
            default -> throw new RuntimeException("Unable to find repository for entity type " + entityTypeStr);
        };
        List<? extends BaseEntityAttributeModel> allAttributes = repo.findBySystemOrSystemIsNull(system);
        //List<BaseEntityAttribute> allAttributes = repositoryMapHelper
        //      .findBySystemOrSystemIsNull(system);

        // 2. Filter only required attributes
        Set<String> mandatoryAttributeNames = allAttributes.stream()
                .filter(BaseEntityAttributeModel::isRequired)
                .map(BaseEntityAttributeModel::getName)
                .collect(Collectors.toSet());

        // 3. Extract all attribute names from the payload
        Set<String> providedAttributeNames = mappings.stream()
                .map(MappingConfigModelCreateDTO::targetAttribute) // or sourceAttribute, depending on your logic
                .collect(Collectors.toSet());

        // 4. Check if any mandatory attribute is missing
        Set<String> missingAttributes = new HashSet<>(mandatoryAttributeNames);
        missingAttributes.removeAll(providedAttributeNames);

        if (!missingAttributes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing mandatory attributes: " + missingAttributes
            );
        }

    }
}
