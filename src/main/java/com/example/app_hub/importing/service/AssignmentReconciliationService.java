package com.example.app_hub.importing.service;

import com.example.app_hub.importing.config.ResolvedEntitySchema;
import com.example.app_hub.system.model.SystemModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AssignmentReconciliationService {

    private final JdbcTemplate jdbcTemplate;

    public AssignmentReconciliationService (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void performReconciliation (SystemModel system, ResolvedEntitySchema schema, Long jobId) {

        markNewStagingRows(schema, system);
        markChangedStagingRows(schema, system);
        markUnchangedStagingRows(schema, system);

        markDeletedStagingRows(schema, system);
    }

    private void markNewStagingRows(ResolvedEntitySchema schema, SystemModel system) {
        // Status 1: Exists in Staging but NOT in Hash table
        String sql = String.format("""
                UPDATE %s s
                SET import_status = 1
                WHERE NOT EXISTS (
                    SELECT 1 FROM %s p 
                    WHERE p.businesskey = (s.account_businesskey || '|' || s.entitlement_businesskey)
                      AND p.system_id = ?
                )
                """, schema.stagingTable(), schema.hashTable());

        jdbcTemplate.update(sql, system.getSystemId());
    }

    private void markChangedStagingRows(ResolvedEntitySchema schema, SystemModel system) {
        // Status 3: Business Key matches, but the row_hash is different
        String sql = String.format("""
                UPDATE %s s
                SET import_status = 3
                FROM %s p
                WHERE p.businesskey = (s.account_businesskey || '|' || s.entitlement_businesskey)
                  AND p.system_id = ?
                  AND s.row_hash != p.row_hash
                """, schema.stagingTable(), schema.hashTable());

        jdbcTemplate.update(sql, system.getSystemId());
    }

    private void markUnchangedStagingRows(ResolvedEntitySchema schema, SystemModel system) {
        // Status 2: Both Business Key AND row_hash match
        String sql = String.format("""
                UPDATE %s s
                SET import_status = 2
                FROM %s p
                WHERE p.businesskey = (s.account_businesskey || '|' || s.entitlement_businesskey)
                  AND p.system_id = ?
                  AND s.row_hash = p.row_hash
                """, schema.stagingTable(), schema.hashTable());

        jdbcTemplate.update(sql, system.getSystemId());
    }

    private void markDeletedStagingRows(ResolvedEntitySchema schema, SystemModel system) {
        // Status 5: Exist in Hash table but missing from current Staging file
        // We insert these into staging as "tombstones" so the promoter knows to delete them
        String sql = String.format("""
                INSERT INTO %1$s (account_businesskey, entitlement_businesskey, import_status, row_hash)
                SELECT 
                    SPLIT_PART(p.businesskey, '|', 1), 
                    SPLIT_PART(p.businesskey, '|', 2), 
                    5,
                    p.row_hash
                FROM %2$s p
                WHERE p.system_id = ?
                AND NOT EXISTS (
                    SELECT 1 FROM %1$s s 
                    WHERE (s.account_businesskey || '|' || s.entitlement_businesskey) = p.businesskey
                )
                """, schema.stagingTable(), schema.hashTable());

        jdbcTemplate.update(sql, system.getSystemId());
    }
}
