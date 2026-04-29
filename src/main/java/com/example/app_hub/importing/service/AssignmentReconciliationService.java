package com.example.app_hub.importing.service;

import com.example.app_hub.importing.config.ResolvedEntitySchema;
import com.example.app_hub.system.model.SystemModel;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AssignmentReconciliationService {

    private final JdbcTemplate jdbcTemplate;

    public AssignmentReconciliationService (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void performReconciliation (SystemModel system, ResolvedEntitySchema schema, Long jobId) {

        markNewStagingRows(schema, system);
        markChangedStagingRows(schema, system);
        markUnchangedStagingRows(schema, system);

        markDeletedStagingRows(schema, system);
    }

    private void markNewStagingRows(ResolvedEntitySchema schema, SystemModel system) {
        String sql = String.format("""
            UPDATE %s s
            SET import_status = 1
            WHERE NOT EXISTS (
                SELECT 1 FROM %s p 
                -- Use the two new columns instead of concatenation
                WHERE p.account_bk = s.account_businesskey
                  AND p.entitlement_bk = s.entitlement_businesskey
                  AND p.system_id = ?
            )
            """, schema.stagingTable(), schema.hashTable());

        jdbcTemplate.update(sql, system.getSystemId());
    }

    private void markChangedStagingRows(ResolvedEntitySchema schema, SystemModel system) {
        String sql = String.format("""
                UPDATE %1$s s
                SET import_status = 3
                FROM %2$s p
                WHERE p.account_bk = s.account_businesskey
                  AND p.entitlement_bk = s.entitlement_businesskey
                  AND p.system_id = ?
                  AND s.row_hash != p.row_hash
                """, schema.stagingTable(), schema.hashTable());

        jdbcTemplate.update(sql, system.getSystemId());
    }

    private void markUnchangedStagingRows(ResolvedEntitySchema schema, SystemModel system) {
        String sql = String.format("""
                UPDATE %1$s s
                SET import_status = 2
                FROM %2$s p
                WHERE p.account_bk = s.account_businesskey
                  AND p.entitlement_bk = s.entitlement_businesskey
                  AND p.system_id = ?
                  AND s.row_hash = p.row_hash
                """, schema.stagingTable(), schema.hashTable());

        jdbcTemplate.update(sql, system.getSystemId());
    }

    private void markDeletedStagingRows(ResolvedEntitySchema schema, SystemModel system) {
        String sql = String.format("""
            INSERT INTO %1$s (account_businesskey, entitlement_businesskey, import_status, row_hash)
            SELECT p.account_bk, p.entitlement_bk, 5, p.row_hash
            FROM %2$s p
            WHERE p.system_id = ?
            AND NOT EXISTS (
                SELECT 1 FROM %1$s s 
                WHERE s.account_businesskey = p.account_bk 
                  AND s.entitlement_businesskey = p.entitlement_bk
            )
            """, schema.stagingTable(), schema.hashTable());

        jdbcTemplate.update(sql, system.getSystemId());
    }
}
