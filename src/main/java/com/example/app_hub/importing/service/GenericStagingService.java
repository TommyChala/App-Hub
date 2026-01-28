package com.example.app_hub.importing.service;

import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.common.utility.CsvDataReader;
import com.example.app_hub.common.utility.SqlUtils;
import com.example.app_hub.mappingconfig.service.MappingExpressionEngine;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GenericStagingService {

    private final JdbcTemplate jdbcTemplate;

    public GenericStagingService(
            JdbcTemplate jdbcTemplate,
            CsvDataReader csvDataReader,
            MappingExpressionEngine mappingExpressionEngine
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void executeDDL(String ddlStatement) {
        jdbcTemplate.execute(ddlStatement);
    }

    public void persistBatch(
            String tableName,
            List<Map<String, String>> batch,
            String[] sourceHeaders,
            Map<String, DataType> headerDataTypes,
            List<String> insertColumns,
            Map<String, String> sourceToTargetColumnName
    ) {
        String columns = String.join(", ", insertColumns);
        String placeholders = String.join(", ", Collections.nCopies(insertColumns.size(), "?"));

        final String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName, columns, placeholders
        );

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, String> row = batch.get(i);

                for (int columnIndex = 0; columnIndex < insertColumns.size(); columnIndex++) {
                    String targetColumn = insertColumns.get(columnIndex);
                    String value;

                    // CLEAN FIX: Handle both pre-transformed data and raw CSV data
                    if (sourceToTargetColumnName != null) {
                        // Logic for raw data: find which CSV header maps to this SQL column
                        String sourceHeader = sourceToTargetColumnName.entrySet().stream()
                                .filter(entry -> entry.getValue().equalsIgnoreCase(targetColumn))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Missing source mapping for target column: " + targetColumn));

                        value = row.get(sourceHeader.toLowerCase());
                    } else {
                        // Logic for transformed data: row keys ALREADY match targetColumn names
                        value = row.get(targetColumn);
                    }

                    // Set value with type safety
                    if (value == null || value.isBlank()) {
                        ps.setNull(columnIndex + 1, Types.VARCHAR);
                    } else {
                        // Use headerDataTypes if available, otherwise default to String
                        DataType type = (headerDataTypes != null) ? headerDataTypes.get(targetColumn) : DataType.STRING;
                        setTypedValue(ps, columnIndex + 1, value, type);
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });
    }

    private void setTypedValue(PreparedStatement ps, int index, String value, DataType type) throws SQLException {
        if (type == null) {
            ps.setString(index, value);
            return;
        }

        try {
            switch (type) {
                case INTEGER -> ps.setInt(index, Integer.parseInt(value.trim()));
                case BOOLEAN -> ps.setBoolean(index, Boolean.parseBoolean(value.trim()));
                case FLOAT -> ps.setDouble(index, Double.parseDouble(value.trim()));
                // If you have a DATE type, you can add parsing logic here
                default -> ps.setString(index, value);
            }
        } catch (Exception e) {
            // Fallback to String if parsing fails to avoid crashing the whole batch
            ps.setString(index, value);
        }
    }

    public void generateStagingHashes(String tableName, Set<String> usedAttributes) {
        String concatExpression = usedAttributes.stream()
                .map(attr -> "COALESCE(" + SqlUtils.safeColumnName(attr) + "::text, '')")
                .collect(Collectors.joining(", '|', "));

        String sql = String.format(
                "UPDATE %s SET row_hash = encode(sha256(concat_ws('|', %s)::bytea), 'hex')",
                tableName, concatExpression
        );

        jdbcTemplate.execute(sql);
    }

    public void markUnchangedRows(String stagingTable, String productionTable, String businessKey) {
        String sql = String.format("""
        UPDATE %s stg
        SET import_status = 4
        FROM %s prod
        WHERE stg.%s = prod.%s
        AND stg.row_hash = prod.row_hash
        """, stagingTable, productionTable, businessKey, businessKey);

        jdbcTemplate.execute(sql);
    }

    public void markNewRecords(String stagingTable, String productionTable, String businessKey) {
        String sql = String.format("""
                UPDATE %1$s stg
                SET import_status = 1
                FROM (
                    SELECT stg_inner.%3$s
                    FROM %1$s stg_inner
                    LEFT JOIN %2$s prod ON stg_inner.%3$s = prod.%3$s
                    WHERE prod.%3$s IS NULL
                ) as new_records
                WHERE stg.%3$s = new_records.%3$s
                """, stagingTable, productionTable, SqlUtils.safeColumnName(businessKey));

        jdbcTemplate.execute(sql);
    }
}
