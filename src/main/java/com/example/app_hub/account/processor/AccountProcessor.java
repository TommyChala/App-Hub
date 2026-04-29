package com.example.app_hub.account.processor;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.exception.ProcessorSetupException;
import com.example.app_hub.common.processor.EntityProcessor;
import com.example.app_hub.common.utility.CSVFileValidator;
import com.example.app_hub.common.utility.CsvDataReader;
import com.example.app_hub.common.utility.SqlUtils;
import com.example.app_hub.importing.config.EntitySchemaRegistry;
import com.example.app_hub.importing.config.ResolvedEntitySchema;
import com.example.app_hub.importing.factory.ProcessingContextFactory;
import com.example.app_hub.importing.service.EntityPromotionService;
import com.example.app_hub.importing.service.GenericStagingService;
import com.example.app_hub.importing.service.ReconciliationService;
import com.example.app_hub.importing.utility.StagingTableManager;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.mappingconfig.service.MappingExpressionEngine;
import com.example.app_hub.system.model.SystemModel;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AccountProcessor implements EntityProcessor<AccountProcessingContext> {

    private final CsvDataReader csvDataReader;
    private final GenericStagingService stagingService;
    private final SqlUtils sqlUtils;
    private final MappingExpressionEngine mappingExpressionEngine;
    private final ProcessingContextFactory contextFactory;
    private final StagingTableManager stagingTableManager;


    public AccountProcessor(
            CsvDataReader csvDataReader,
            GenericStagingService stagingService,
            SqlUtils sqlUtils,
            ReconciliationService reconciliationService,
            MappingExpressionEngine mappingExpressionEngine,
            ProcessingContextFactory contextFactory,
            StagingTableManager stagingTableManager,
            EntitySchemaRegistry entitySchemaRegistry,
            EntityPromotionService promotionService
    ) {
        this.csvDataReader = csvDataReader;
        this.stagingService = stagingService;
        this.sqlUtils = sqlUtils;
        this.mappingExpressionEngine = mappingExpressionEngine;
        this.contextFactory = contextFactory;
        this.stagingTableManager = stagingTableManager;
    }

    @Override
    public EntityType getType() {
        return EntityType.ACCOUNT;
    }

    @Override
    @Transactional
    public void process(File file, SystemModel system, EntityType entityType)
            throws IOException, CsvValidationException {

        validateInputs(file, system);
        AccountProcessingContext context = contextFactory.buildAccountContext(system);

        String[] rawCsvHeaders = csvDataReader.readHeaders(file);

        if (!CSVFileValidator.validateHeadersAgainstMapping(rawCsvHeaders, context.activeMappings())) {
            System.err.println("❌ Validation failed in CSVFileValidator");
            throw new IllegalArgumentException("CSV missing required columns for DIRECT mappings.");
        }
        String stagingTableName;

        try {
            stagingTableName = stagingTableManager.getStagingTableName(entityType, system);
            stagingTableManager.prepareStagingTableAndReturnStagingName(entityType, system, context, stagingTableName);
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to build stagingTable" + e);
        }

        try {
            System.out.println("Step4: Starting Stream and Persist");
            streamAndPersist(file, stagingTableName, rawCsvHeaders, context);
            sqlUtils.generateAttributeHashes(stagingTableName, context.usedAttributes(), entityType);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Unable to create staging table: "+ ex);
        }

    }

    private void validateInputs(File file, SystemModel system) {
        if (file == null) {
            throw new ProcessorSetupException("Cannot process: File is missing or null");
        }
        if (system == null) {
            throw new ProcessorSetupException("Cannot process: System is null");
        }
    }

    private void streamAndPersist(File file, String tableName, String[] headers, AccountProcessingContext ctx)
            throws IOException, CsvValidationException {

        int BATCH_SIZE = 500;
        final List<Map<String, String>> transformedBatch = new ArrayList<>(BATCH_SIZE);

        System.out.println("DEBUG: Entering streamAndPersist. File size: " + file.length());

        try {
            csvDataReader.streamCsv(file, headers, csvRow -> {
                try {
                    // If this prints, we know the loop is working
                    if (transformedBatch.isEmpty()) {
                        System.out.println("DEBUG: Processing very first row...");
                    }

                    Map<String, String> transformedRow = new HashMap<>();

                    for (MappingConfigModel mapping : ctx.activeMappings()) {
                        String targetColName = SqlUtils.safeColumnName(mapping.getTargetAttribute().getName());
                        Object calculatedValue = mappingExpressionEngine.calculateValue(mapping, csvRow);
                        transformedRow.put(targetColName, calculatedValue != null ? calculatedValue.toString() : null);
                    }

                    transformedBatch.add(transformedRow);
                    if (transformedBatch.size() >= BATCH_SIZE) {
                        flush(tableName, transformedBatch, ctx);
                    }
                } catch (Exception rowEx) {
                    System.err.println("ERROR inside CSV row processing: " + rowEx.getMessage());
                    rowEx.printStackTrace();
                }
            });
        } catch (Exception streamEx) {
            System.err.println("ERROR during CSV streaming: " + streamEx.getMessage());
            streamEx.printStackTrace();
            throw streamEx;
        }

        System.out.println("DEBUG: Finished streamCsv loop. Batch size remaining: " + transformedBatch.size());

        if (!transformedBatch.isEmpty()) {
            flush(tableName, transformedBatch, ctx);
            System.out.println("DEBUG: Final flush complete.");
        }
    }

    private void flush(String table, List<Map<String, String>> batch, AccountProcessingContext ctx) {
        // We pass NULL for sourceToTarget because the batch is already transformed
        stagingService.persistBatch(
                table,
                batch,
                ctx.insertColumns().toArray(new String[0]),
                ctx.targetDataTypes(),
                ctx.insertColumns(),
                null
        );
        batch.clear();
    }
}
