package com.example.app_hub.importing.factory;

import com.example.app_hub.account.model.AccountAttributeModel;
import com.example.app_hub.account.processor.AccountProcessingContext;
import com.example.app_hub.assignment.processor.EntitlementAssignmentProcessingContext;
import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.common.utility.RepositoryMapHelper;
import com.example.app_hub.common.utility.SqlUtils;
import com.example.app_hub.entitlement.model.EntitlementAttributeModel;
import com.example.app_hub.entitlement.processor.EntitlementProcessingContext;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.mappingconfig.repository.MappingConfigModelRepository;
import com.example.app_hub.system.model.SystemModel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProcessingContextFactory {

    private final MappingConfigModelRepository mappingRepo;
    private final RepositoryMapHelper repositoryMapHelper;

    public ProcessingContextFactory (
            MappingConfigModelRepository mappingRepo,
            RepositoryMapHelper repositoryMapHelper
    )
    {
        this.mappingRepo = mappingRepo;
        this.repositoryMapHelper = repositoryMapHelper;
    }

    public EntitlementAssignmentProcessingContext buildAssignmentContext(SystemModel system) {
        List<MappingConfigModel> mappings = mappingRepo.findByEntityTypeAndSystemOrSystemIsNull(EntityType.ASSIGNMENT, system);


        if (mappings.stream()
                .filter(m -> m.getTargetAttribute().getEntityType() == EntityType.ACCOUNT)
                .toList().size() != 1) {
            throw new RuntimeException("Number of mappings configured for entity type account must be exactly 1");
        }
        if (mappings.stream()
                .filter(m -> m.getTargetAttribute().getEntityType() == EntityType.ENTITLEMENT)
                .toList().size() != 1) {
            throw new RuntimeException("Number of mappings configured for entity type entitlement must be exactly 1");
        }



        Map<EntityType, BaseEntityAttributeModel> attributeReferences = mappings.stream()
                .map(MappingConfigModel::getTargetAttribute)
                .collect(Collectors.toMap(BaseEntityAttributeModel::getEntityType, attr -> attr)
                );

        List<BaseEntityAttributeModel> attributes = new ArrayList<>(attributeReferences.values());

        validateBaseNeeds(system, mappings, attributes);

        List<String> insertColumns = new ArrayList<>();
        Map<String, DataType> targetDataTypes = new HashMap<>();
        Set<String> usedAttributeNames = new LinkedHashSet<>();

        attributeReferences.forEach((entityType, currentAttribute) ->  {
            if (currentAttribute == null) {
                throw new RuntimeException("attribute cannot be null");
            }

            String rawColumnName = SqlUtils.safeColumnName(currentAttribute.getName());
            String prefixedColumnName;

            if (entityType == EntityType.ACCOUNT) {
                prefixedColumnName = "account_" + rawColumnName;
            }
            else if (entityType == EntityType.ENTITLEMENT) {
                prefixedColumnName = "entitlement_" + rawColumnName;
            }
            else {
                throw new RuntimeException("EntityType not recognized: " + entityType);
            }
            insertColumns.add(prefixedColumnName);
            targetDataTypes.put(prefixedColumnName, currentAttribute.getDataType());
            usedAttributeNames.add(currentAttribute.getName());
        });
        return new EntitlementAssignmentProcessingContext(mappings,targetDataTypes,insertColumns,usedAttributeNames);
    }

    public AccountProcessingContext buildAccountContext(SystemModel system) {
        List<MappingConfigModel> mappings = mappingRepo.findByEntityTypeAndSystemOrSystemIsNull(EntityType.ACCOUNT, system);
        List<AccountAttributeModel> attributes = repositoryMapHelper.getAccountAttributeRepository().findBySystemOrSystemIsNull(system);

        validateBaseNeeds(system, mappings, attributes);

        Map<UUID, AccountAttributeModel> attrById = attributes.stream()
                .collect(Collectors.toMap(AccountAttributeModel::getId, Function.identity()));

        Map<String, DataType> targetDataTypes = new HashMap<>();
        List<String> insertColumns = new ArrayList<>();
        Set<String> usedAttributeNames = new LinkedHashSet<>();

        for (MappingConfigModel m : mappings) {
            AccountAttributeModel targetAttr = attrById.get(m.getTargetAttribute().getId());
            if (targetAttr != null) {
                String safeName = SqlUtils.safeColumnName(targetAttr.getName());
                insertColumns.add(safeName);
                targetDataTypes.put(safeName, targetAttr.getDataType());
                usedAttributeNames.add(targetAttr.getName());
            }
        }
        return new AccountProcessingContext(mappings, targetDataTypes, insertColumns, usedAttributeNames, attributes);
    }

    public EntitlementProcessingContext buildEntitlementContext(SystemModel system) {
        List<MappingConfigModel> mappings = mappingRepo.findByEntityTypeAndSystemOrSystemIsNull(EntityType.ENTITLEMENT, system);
        List<EntitlementAttributeModel> attributes = repositoryMapHelper.getEntitlementAttributeRepository().findBySystemOrSystemIsNull(system);

        validateBaseNeeds(system, mappings, attributes);

        Map<UUID, EntitlementAttributeModel> attrById = attributes.stream()
                .collect(Collectors.toMap(EntitlementAttributeModel::getId, Function.identity()));

        Map<String, DataType> targetDataTypes = new HashMap<>();
        List<String> insertColumns = new ArrayList<>();
        Set<String> usedAttributeNames = new LinkedHashSet<>();

        for (MappingConfigModel m : mappings) {
            EntitlementAttributeModel targetAttr = attrById.get(m.getTargetAttribute().getId());
            if (targetAttr != null) {
                String safeName = SqlUtils.safeColumnName(targetAttr.getName());
                insertColumns.add(safeName);
                targetDataTypes.put(safeName, targetAttr.getDataType());
                usedAttributeNames.add(targetAttr.getName());
            }
        }
        return new EntitlementProcessingContext(mappings, targetDataTypes, insertColumns, usedAttributeNames, attributes);
    }

    private void validateBaseNeeds(SystemModel system, List<?> mappings, List<?> attributes) {
        if (mappings.isEmpty()) throw new RuntimeException("No mappings for system: " + system.getName());
        if (attributes.isEmpty()) throw new IllegalStateException("No attributes for system: " + system.getName());
    }
}

