package com.example.app_hub.importing.service;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.importing.config.ResolvedEntitySchema;
import com.example.app_hub.system.model.SystemModel;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReconciliationService {

    private final JdbcTemplate jdbcTemplate;

    public ReconciliationService(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void performReconciliation(SystemModel system, ResolvedEntitySchema schema, Long jobId, EntityType entityType) {

        markNewStagingRows(schema, system);
        markChangedStagingRows(schema, system);
        markUnchangedStagingRows(schema, system);
        markDeletedStagingRows(schema, system);

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
}
