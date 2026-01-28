package com.example.app_hub.importing.service;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.exception.PromotionException;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.common.repository.BaseEntityAttributeModelRepository;
import com.example.app_hub.importing.config.EntitySchemaRegistry;
import com.example.app_hub.importing.config.ResolvedEntitySchema;
import com.example.app_hub.system.model.SystemModel;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.example.app_hub.common.entitytype.EntityType.ACCOUNT;
import static com.example.app_hub.common.entitytype.EntityType.ENTITLEMENT;

@Service
public class AssignmentPromotionService {

    private final EntitySchemaRegistry entitySchemaRegistry;
    private final JdbcTemplate jdbcTemplate;

    public AssignmentPromotionService(EntitySchemaRegistry entitySchemaRegistry, JdbcTemplate jdbcTemplate) {
        this.entitySchemaRegistry = entitySchemaRegistry;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void promoteAssignments(EntityType entityType, SystemModel system) {

        ResolvedEntitySchema schema = entitySchemaRegistry.resolve(entityType, system);

        if (entityType != EntityType.ASSIGNMENT) {
            throw new RuntimeException("EntityType not recognized!");
        }
        try {
            softDeletedAssignments(schema, system);
            promoteNewAssignments(schema, system);
            updateProductionHashes(system, schema);
        } catch (DataAccessException e) {
            throw new PromotionException("Unable to promote staging data to production", e);
        } catch (Exception e) {
            throw new PromotionException("Critical system error encountered during " +
                    "promotion from staging to production", e);
        }
    }

    private void softDeletedAssignments(ResolvedEntitySchema schema, SystemModel system) {
        UUID systemId = system.getId();

        // 1. Fetch the Attribute IDs for the lookup
        UUID accountBusinessKey = getEntityBusinessKey(ACCOUNT);
        UUID entitlementBusinessKey = getEntityBusinessKey(ENTITLEMENT);

        // 2. Perform Soft Delete (UPDATE instead of DELETE)
        String sql = String.format("""
        UPDATE %s 
        SET is_current = false 
        WHERE system_id = ? 
        AND is_current = true
        AND (account_id, entitlement_id) IN (
            SELECT a.uid, e.uid
            FROM %s s
            JOIN account a ON a.system_id = ?
            JOIN account_attribute_value_model av ON av.account_id = a.uid 
                AND av.attribute_id = ? 
                AND av.value_string = s.account_businesskey
            JOIN entitlement e ON e.system_id = ?
            JOIN entitlement_attribute_value_model ev ON ev.entitlement_id = e.uid 
                AND ev.attribute_id = ? 
                AND ev.value_string = s.entitlement_businesskey
            WHERE s.import_status = 5
        )
        """, schema.entityProductionTable(), schema.stagingTable());

        int count = jdbcTemplate.update(sql,
                systemId, // Outer WHERE
                systemId, accountBusinessKey, // Account Join
                systemId, entitlementBusinessKey // Entitlement Join
        );

        if (count > 0) {
            System.out.println("⚠️ Soft-deleted " + count + " assignments (is_current set to false).");
        }
    }

    private void promoteNewAssignments(ResolvedEntitySchema schema, SystemModel system) {

        UUID accountBusinessKey = getEntityBusinessKey(ACCOUNT);
        UUID entitlementBusinessKey = getEntityBusinessKey(ENTITLEMENT);

        String sql = String.format("""
                INSERT INTO %s (id, account_id, entitlement_id, system_id, is_current)
                SELECT 
                    gen_random_uuid(), 
                    a.uid, 
                    e.uid, 
                    ?,
                    true
                FROM %s s
                -- Lookup Account
                JOIN account a ON a.system_id = ?
                JOIN account_attribute_value_model av ON av.account_id = a.uid 
                    AND av.attribute_id = ? 
                    AND av.value_string = s.account_businesskey
                    AND av.is_row_latest = true
                -- Lookup Entitlement
                JOIN entitlement e ON e.system_id = ?
                JOIN entitlement_attribute_value_model ev ON ev.entitlement_id = e.uid 
                    AND ev.attribute_id = ? 
                    AND ev.value_string = s.entitlement_businesskey
                    AND ev.is_row_latest = true
                WHERE s.import_status IN (1, 3)
                ON CONFLICT (account_id, entitlement_id)
                DO UPDATE SET is_current = true
                """, schema.entityProductionTable(), schema.stagingTable());

        int count = jdbcTemplate.update(sql,
                system.getId(),
                system.getId(), accountBusinessKey,
                system.getId(), entitlementBusinessKey
        );

        System.out.println("Promoted " + count + " assignments successfully.");
    }

    private void updateProductionHashes(SystemModel system, ResolvedEntitySchema schema) {

        //ResolvedEntitySchema schema = entitySchemaRegistry.resolve(type, system);

        String sql = String.format("""
            INSERT INTO %s (businesskey, system_id, row_hash)
            SELECT (s.account_businesskey || '|' || s.entitlement_businesskey), ?, s.row_hash
            FROM %s s
            WHERE s.import_status IN (1, 3)
            ON CONFLICT (businesskey, system_id) 
            DO UPDATE SET row_hash = EXCLUDED.row_hash
            """, schema.hashTable(), schema.stagingTable());

        jdbcTemplate.update(sql, system.getSystemId());

        String purgeSql = String.format("""
        DELETE FROM %s
        WHERE system_id = ?
        AND businesskey IN (
            SELECT (s.account_businesskey || '|' || s.entitlement_businesskey)
            FROM %s s
            WHERE s.import_status = 5
        )
        """, schema.hashTable(), schema.stagingTable());

        int purgedCount = jdbcTemplate.update(purgeSql, system.getSystemId());

        if (purgedCount > 0) {
            System.out.println("Cleaned up " + purgedCount + " hashes for soft-deleted assignments.");
        }
    }

    private UUID getEntityBusinessKey(EntityType entityType) {

        String attributeModelTable = switch (entityType) {
            case ACCOUNT -> "account_attribute_model";
            case ENTITLEMENT -> "entitlement_attribute_model";
            default -> throw new RuntimeException("EntityType not allowed for business key lookup: " + entityType);
        };

        // We join the base attributes with the specific model to get the correct UUID
        String sql = String.format("""
                SELECT ea.id
                FROM base_entity_attributes ea
                JOIN %s model ON model.id = ea.id
                WHERE ea.name = 'businesskey' -- Or 'entitlement_id', or whatever your BK name is
                """, attributeModelTable);

        try {
            return jdbcTemplate.queryForObject(sql, UUID.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not find Business Key attribute for " + entityType, e);
        }
    }
}
