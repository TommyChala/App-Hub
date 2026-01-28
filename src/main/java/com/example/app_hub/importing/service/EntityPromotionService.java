package com.example.app_hub.importing.service;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.exception.PromotionException;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.common.utility.SqlUtils;
import com.example.app_hub.importing.config.EntitySchemaRegistry;
import com.example.app_hub.importing.config.ResolvedEntitySchema;
import com.example.app_hub.system.model.SystemModel;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntityPromotionService {

    private final ReconciliationService reconciliationService;
    private final EntitySchemaRegistry entitySchemaRegistry;
    private final JdbcTemplate jdbcTemplate;

    public EntityPromotionService(ReconciliationService reconciliationService, EntitySchemaRegistry entitySchemaRegistry, JdbcTemplate jdbcTemplate) {
        this.reconciliationService = reconciliationService;
        this.entitySchemaRegistry = entitySchemaRegistry;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void promoteEntities (EntityType entityType, SystemModel system, List<? extends BaseEntityAttributeModel> attributes) {

        ResolvedEntitySchema schema = entitySchemaRegistry.resolve(entityType, system);

        if (entityType != EntityType.ACCOUNT && entityType != EntityType.ENTITLEMENT) {
            throw new RuntimeException("EntityType not recognized!");
        }
        try {
            softDeleteEntities(entityType, system, schema);
            promoteNewBaseEntities(entityType, system, schema);
            promoteAttributes(entityType, system, attributes, schema);
            updateProductionHashes(entityType, system, schema);
        } catch (DataAccessException e) {
            throw new PromotionException("Unable to promote staging data to production", e);
        } catch (Exception e) {
            throw new PromotionException("Critical system error encountered during " +
                    "promotion from staging to production", e);
        }
    }

    private void promoteNewBaseEntities(EntityType type, SystemModel system, ResolvedEntitySchema schema) {

        //ResolvedEntitySchema schema = entitySchemaRegistry.resolve(type, system);

        //String entity = type.toString().toLowerCase();

        String sql = String.format("""
            INSERT INTO %s (uid, %s, system_id, is_current)
            SELECT gen_random_uuid(), s.%s, ?, true
            FROM %s s
            WHERE s.import_status = 1
            ON CONFLICT (%s) DO UPDATE SET is_current = true
            """, schema.entityProductionTable(), schema.prodIdCol(), schema.businessKeyCol(), schema.stagingTable(), schema.prodIdCol());

        int count = jdbcTemplate.update(sql, system.getId());
        System.out.println("Promoted " + count + " new records to " + schema.entityProductionTable());
    }
    //public void promoteAttributes(String stagingTable, SystemModel system, List<? extends BaseEntityAttribute> attributes,
    //                             String valueTable, String ownerTable, String ownerJoinColumn, String businessKeyCol) {
    private void promoteAttributes(EntityType type, SystemModel system, List<? extends BaseEntityAttributeModel> attributes, ResolvedEntitySchema schema) {

        //ResolvedEntitySchema schema = entitySchemaRegistry.resolve(type, system);
        String attributeValueModel = switch (type) {
            case ACCOUNT -> "account_attribute_value_model";
            case ENTITLEMENT -> "entitlement_attribute_value_model";
            default -> throw new RuntimeException("not able to generate name for valuemodel");
        };

        for (BaseEntityAttributeModel attr : attributes) {
            String targetColumn = "value_string";
            String castType = "TEXT";

            switch (attr.getDataType()) {
                case INTEGER -> { targetColumn = "value_integer"; castType = "INTEGER"; }
                case DATE -> { targetColumn = "value_datetime"; castType = "TIMESTAMP"; }
                case FLOAT -> { targetColumn = "value_float"; castType = "DOUBLE PRECISION"; }
                case BOOLEAN -> { targetColumn = "value_boolean"; castType = "BOOLEAN"; }
            }

            String sql = String.format("""
            INSERT INTO %1$s (id, %2$s, attribute_id, %3$s, is_row_latest)
            SELECT gen_random_uuid(), a.uid, ?, CAST(s.%4$s AS %5$s), true
            FROM %6$s s
            JOIN %7$s a ON s.%8$s = a.%2$s -- Joining staging BK to production ID col
            WHERE s.import_status IN (1, 3)
              AND a.system_id = ?
            ON CONFLICT (%2$s, attribute_id)
            DO UPDATE SET %3$s = EXCLUDED.%3$s, is_row_latest = true
            """,
                    attributeValueModel,            // %1$s
                    schema.ownerJoinColumn(),       // %2$s
                    targetColumn,                   // %3$s
                    SqlUtils.safeColumnName(attr.getName()), // %4$s
                    castType,                       // %5$s
                    schema.stagingTable(),          // %6$s
                    schema.ownerTable(),            // %7$s
                    schema.businessKeyCol()         // %8$s
            );

            jdbcTemplate.update(sql, attr.getId(), system.getId());
        }
    }

    private void softDeleteEntities(EntityType type, SystemModel system, ResolvedEntitySchema schema) {
        String sql = String.format("""
            UPDATE %1$s p
            SET is_current = false
            WHERE p.system_id = ? 
            AND p.is_current = true
            AND p.%2$s IN (
                SELECT s.%3$s 
                FROM %4$s s 
                WHERE s.import_status = 5
            )
            """,
                schema.entityProductionTable(), // %1$s
                schema.prodIdCol(),             // %2$s
                schema.businessKeyCol(),        // %3$s
                schema.stagingTable()           // %4$s
        );

        int count = jdbcTemplate.update(sql, system.getId());
        if (count > 0) {
            System.out.println("🗑️ Soft-deleted " + count + " " + type + " records.");
        }
    }

    //public void updateProductionHashes(String stagingTable, SystemModel system, String businessKeyCol, String hashTable) {
    private void updateProductionHashes(EntityType type, SystemModel system, ResolvedEntitySchema schema) {

        //ResolvedEntitySchema schema = entitySchemaRegistry.resolve(type, system);

        String sql = String.format("""
            INSERT INTO %s (businesskey, system_id, row_hash)
            SELECT s.%s, ?, s.row_hash
            FROM %s s
            WHERE s.import_status IN (1, 3)
            ON CONFLICT (businesskey, system_id) 
            DO UPDATE SET row_hash = EXCLUDED.row_hash
            """, schema.hashTable(), schema.businessKeyCol(), schema.stagingTable());

        jdbcTemplate.update(sql, system.getSystemId());

        String purgeSql = String.format("""
        DELETE FROM %s
        WHERE system_id = ?
        AND businesskey IN (
            SELECT s.businesskey
            FROM %s s
            WHERE s.import_status = 5
        )
        """, schema.hashTable(), schema.stagingTable());

        int purgedCount = jdbcTemplate.update(purgeSql, system.getSystemId());

        if (purgedCount > 0) {
            System.out.println("Cleaned up " + purgedCount + " hashes for soft-deleted entities.");
        }
    }
}
