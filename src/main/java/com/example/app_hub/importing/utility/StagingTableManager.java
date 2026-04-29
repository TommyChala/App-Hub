package com.example.app_hub.importing.utility;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.processor.ProcessingContext;
import com.example.app_hub.common.utility.SqlUtils;
import com.example.app_hub.importing.service.GenericStagingService;
import com.example.app_hub.system.model.SystemModel;
import org.springframework.stereotype.Component;

@Component
public class StagingTableManager {

    private final GenericStagingService stagingService;

    public StagingTableManager(GenericStagingService stagingService) {
        this.stagingService = stagingService;
    }

    public String getStagingTableName (EntityType entityType, SystemModel system) {
        return SqlUtils.getStagingTableName(entityType, system);
    }

    public void prepareStagingTableAndReturnStagingName(EntityType entityType, SystemModel system, ProcessingContext context, String tableName) {

        stagingService.executeDDL("DROP TABLE IF EXISTS " + tableName);

        // 2. Build the CREATE TABLE statement
        StringBuilder sql = new StringBuilder("""
                    CREATE TABLE %s (
                        staging_id SERIAL PRIMARY KEY,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        import_status INT DEFAULT 0,
                        row_hash char(64)
                """.formatted(tableName));

        for (String columnName : context.insertColumns()) {
            sql.append(", ").append(columnName).append(" VARCHAR(255)");
        }

        sql.append("\n);");

        try {
            stagingService.executeDDL(sql.toString());
        } catch (Exception e) {
            throw new RuntimeException("stagingTable creation failed" + e);
        }

    }
}
