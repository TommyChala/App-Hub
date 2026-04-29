package com.example.app_hub.importing.config;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.utility.SqlUtils;
import com.example.app_hub.system.model.SystemModel;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EntitySchemaRegistry {

    private final Map<EntityType, EntityMetaData> staticMetadata = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {

        staticMetadata.put(EntityType.ACCOUNT, new EntityMetaData(
                "businesskey",
                "account_id",
                "account_id",
                "account_production_hashes",
                "account"
        ));

        staticMetadata.put(EntityType.ENTITLEMENT, new EntityMetaData(
                "businesskey",
                "entitlement_id",
                "entitlement_id",
                "entitlement_production_hashes",
                "entitlement"
        ));

        staticMetadata.put(EntityType.ASSIGNMENT, new EntityMetaData(
                "account_businesskey",
                "account_id",
                "id",
                "assignment_production_hashes",
                "entitlement_assignment"
        ));
    }

    public ResolvedEntitySchema resolve(EntityType type, SystemModel system) {
            EntityMetaData meta = staticMetadata.get(type);

            if (meta == null) {
                throw new IllegalArgumentException("No metadata registered for EntityType." +
                        "Please check EntitySchemaRegistry.init()");
            }

            return new ResolvedEntitySchema(
                    SqlUtils.getStagingTableName(type, system),
                    SqlUtils.getEntityProductionTableName(type),
                    meta.hashTable(),
                    meta.businessKeyCol(),
                    meta.ownerJoinColumn(),
                    meta.prodIdCol(),
                    meta.ownerTable()
            );
    }
}
