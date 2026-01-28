package com.example.app_hub.importing.service;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.exception.PromotionException;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.common.utility.SqlUtils;
import com.example.app_hub.importing.config.EntitySchemaRegistry;
import com.example.app_hub.importing.config.ResolvedEntitySchema;
import com.example.app_hub.importing.observer.ImportSyncObserver;
import com.example.app_hub.system.model.SystemModel;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.util.List;

@Service
public class ReconciliationService {

    private final JdbcTemplate jdbcTemplate;

    public ReconciliationService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

/*
    @Transactional
    public void promoteToProduction (EntityType entityType, SystemModel system, List<? extends BaseEntityAttributeModel> attributes) {

        ResolvedEntitySchema schema = entitySchemaRegistry.resolve(entityType, system);

         if (entityType != EntityType.ACCOUNT && entityType != EntityType.ENTITLEMENT) {
             throw new RuntimeException("EntityType not recognized!");
         }
        try {
            performReconciliation(system, schema);
            //promoteNewBaseEntities(entityType, system, schema);
            //promoteAttributes(entityType, system, attributes, schema);
            //updateProductionHashes(entityType, system, schema);
        } catch (DataAccessException e) {
            throw new PromotionException("Unable to promote staging data to production", e);
        } catch (Exception e) {
            throw new PromotionException("Critical system error encountered during " +
                    "promotion from staging to production", e);
        }
    }

 */

    public void performReconciliation(SystemModel system, ResolvedEntitySchema schema, Long jobId, EntityType entityType) {

        markNewStagingRows(schema, system);
        markChangedStagingRows(schema, system);
        markUnchangedStagingRows(schema, system);
        markDeletedStagingRows(schema, system);

        //syncObserver.provideStatusReport(jobId, system, entityType, "RECONCILED");

        //ResolvedEntitySchema schema = entitySchemaRegistry.resolve(type, system);

        /*
        // 1. Identify UPDATES (Status 3)
        String updateSql = String.format("""
                UPDATE %s s
                SET import_status = 3
                FROM %s p
                WHERE s.%s = p.businesskey
                  AND p.system_id = ?
                  AND s.row_hash != p.row_hash
                """, schema.stagingTable(), schema.hashTable(), schema.businessKeyCol());

        // 2. Identify INSERTS (Status 1)
        String insertSql = String.format("""
                UPDATE %s s
                SET import_status = 1
                WHERE NOT EXISTS (
                    SELECT 1 FROM %s p
                    WHERE LOWER(TRIM(p.businesskey)) = LOWER(TRIM(s.%s))
                      AND p.system_id = ?
                )
                """, schema.stagingTable(), schema.hashTable(), schema.businessKeyCol());

        // 3. Identify UNCHANGED (Status 2)
        String unchangedSql = String.format("""
                UPDATE %s s
                SET import_status = 2
                FROM %s p
                WHERE s.%s = p.businesskey
                  AND p.system_id = ?
                  AND s.row_hash = p.row_hash
                """, schema.stagingTable(), schema.hashTable(), schema.businessKeyCol());

        jdbcTemplate.update(updateSql, system.getSystemId());
        jdbcTemplate.update(insertSql, system.getSystemId());
        jdbcTemplate.update(unchangedSql, system.getSystemId());

         */

    }

    private void markDeletedStagingRows (ResolvedEntitySchema schema, SystemModel system) {
        String sql = String.format("""
                INSERT INTO %1$s (businesskey, import_status, created_at, row_hash)
                SELECT p.businesskey, 5, NOW(), p.row_hash
                FROM %2$s p
                WHERE p.system_id = ?
                AND NOT EXISTS (SELECT 1 FROM %1$s s
                WHERE s.businesskey = p.businesskey)""", schema.stagingTable(), schema.hashTable()
        );

        jdbcTemplate.update(sql, system.getSystemId());
    }
    private void markChangedStagingRows (ResolvedEntitySchema schema, SystemModel system) {
        String updateSql = String.format("""
                UPDATE %s s
                SET import_status = 3
                FROM %s p
                WHERE s.%s = p.businesskey
                  AND p.system_id = ?
                  AND s.row_hash != p.row_hash
                """, schema.stagingTable(), schema.hashTable(), schema.businessKeyCol());

        jdbcTemplate.update(updateSql, system.getSystemId());
    }

    private void markNewStagingRows (ResolvedEntitySchema schema, SystemModel system) {
        String insertSql = String.format("""
                UPDATE %s s
                SET import_status = 1
                WHERE NOT EXISTS (
                    SELECT 1 FROM %s p 
                    WHERE LOWER(TRIM(p.businesskey)) = LOWER(TRIM(s.%s))
                      AND p.system_id = ?
                )
                """, schema.stagingTable(), schema.hashTable(), schema.businessKeyCol());

        jdbcTemplate.update(insertSql, system.getSystemId());
    }

    private void markUnchangedStagingRows (ResolvedEntitySchema schema, SystemModel system) {
        String unchangedSql = String.format("""
                UPDATE %s s
                SET import_status = 2
                FROM %s p
                WHERE s.%s = p.businesskey
                  AND p.system_id = ?
                  AND s.row_hash = p.row_hash
                """, schema.stagingTable(), schema.hashTable(), schema.businessKeyCol());

        jdbcTemplate.update(unchangedSql, system.getSystemId());
    }
    /*

//    public void performReconciliation(String stagingTable, Long systemId, String businessKeyCol, String hashTable) {

    //public void promoteNewBaseEntities(String stagingTable, SystemModel system, String businessKeyCol, String entityProductionTable, String prodIdCol) {
    public void promoteNewBaseEntities(EntityType type, SystemModel system, ResolvedEntitySchema schema) {

        //ResolvedEntitySchema schema = entitySchemaRegistry.resolve(type, system);

        //String entity = type.toString().toLowerCase();

        String sql = String.format("""
            INSERT INTO %s (uid, %s, system_id)
            SELECT gen_random_uuid(), s.%s, ?
            FROM %s s
            WHERE s.import_status = 1
            ON CONFLICT (%s) DO NOTHING
            """, schema.entityProductionTable(), schema.prodIdCol(), schema.businessKeyCol(), schema.stagingTable(), schema.prodIdCol());

        int count = jdbcTemplate.update(sql, system.getId());
        System.out.println("Promoted " + count + " new records to " + schema.entityProductionTable());
    }
    //public void promoteAttributes(String stagingTable, SystemModel system, List<? extends BaseEntityAttribute> attributes,
    //                             String valueTable, String ownerTable, String ownerJoinColumn, String businessKeyCol) {
    public void promoteAttributes(EntityType type, SystemModel system, List<? extends BaseEntityAttributeModel> attributes, ResolvedEntitySchema schema) {

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
                INSERT INTO %s (id, %s, attribute_id, %s, is_row_latest)
                SELECT gen_random_uuid(), a.uid, ?, CAST(s.%s AS %s), true
                FROM %s s
                JOIN %s a ON s.%s = a.%s
                WHERE s.import_status IN (1, 3) 
                  AND a.system_id = ?
                ON CONFLICT (%s, attribute_id) 
                DO UPDATE SET %s = EXCLUDED.%s, is_row_latest = true
                """,
                    attributeValueModel,
                    schema.ownerJoinColumn(), targetColumn,
                    SqlUtils.safeColumnName(attr.getName()), castType,
                    schema.stagingTable(), schema.ownerTable(), schema.businessKeyCol(), schema.ownerJoinColumn().replace("_id", "_id"), // logic assumes col names match
                    schema.ownerJoinColumn(), targetColumn, targetColumn
            );

            jdbcTemplate.update(sql, attr.getId(), system.getId());
        }
    }

    //public void updateProductionHashes(String stagingTable, SystemModel system, String businessKeyCol, String hashTable) {
    public void updateProductionHashes(EntityType type, SystemModel system, ResolvedEntitySchema schema) {

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
    }

     */
}
