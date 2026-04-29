package com.example.app_hub.assignment.processor;

import com.example.app_hub.account.processor.AccountProcessingContext;
import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.exception.ProcessorSetupException;
import com.example.app_hub.common.processor.EntityProcessor;
import com.example.app_hub.common.utility.CSVFileValidator;
import com.example.app_hub.common.utility.CsvDataReader;
import com.example.app_hub.common.utility.SqlUtils;
import com.example.app_hub.importing.factory.ProcessingContextFactory;
import com.example.app_hub.importing.service.GenericStagingService;
import com.example.app_hub.importing.service.ReconciliationService;
import com.example.app_hub.importing.utility.StagingTableManager;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.mappingconfig.repository.MappingConfigModelRepository;
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
public class EntitlementAssignmentProcessor implements EntityProcessor<EntitlementAssignmentProcessingContext> {

    private final MappingConfigModelRepository mappingConfigModelRepository;
    private final CsvDataReader csvDataReader;
    private final ProcessingContextFactory contextFactory;
    private final StagingTableManager stagingTableManager;
    private final GenericStagingService stagingService;
    private final MappingExpressionEngine mappingExpressionEngine;
    private final SqlUtils sqlUtils;

    public EntitlementAssignmentProcessor (
            MappingConfigModelRepository mappingConfigModelRepository,
            CsvDataReader csvDataReader,
            ProcessingContextFactory contextFactory,
            StagingTableManager stagingTableManager,
            GenericStagingService stagingService,
            MappingExpressionEngine mappingExpressionEngine,
            SqlUtils sqlUtils,
            ReconciliationService reconciliationService
    ) {
        this.mappingConfigModelRepository = mappingConfigModelRepository;
        this.csvDataReader = csvDataReader;
        this.contextFactory = contextFactory;
        this.stagingTableManager = stagingTableManager;
        this.stagingService = stagingService;
        this.mappingExpressionEngine = mappingExpressionEngine;
        this.sqlUtils = sqlUtils;
    }

    @Override
    public EntityType getType() {
        return EntityType.ASSIGNMENT;
    }

    @Override
    @Transactional
    public void process(File file, SystemModel system, EntityType entityType)
            throws IOException, CsvValidationException {

        validateInputs(file, system);
        String[] rawCsvHeaders = csvDataReader.readHeaders(file);
        EntitlementAssignmentProcessingContext context = contextFactory.buildAssignmentContext(system);

        if (!CSVFileValidator.validateHeadersAgainstMapping(rawCsvHeaders, context.activeMappings())) {
            System.err.println("❌ Validation failed in CSVFileValidator");
            throw new IllegalArgumentException("CSV missing required columns for DIRECT mappings.");
        }
        System.out.println(context.activeMappings());
        System.out.println(context.insertColumns());
        String stagingTableName;

        try {
            stagingTableName = stagingTableManager.getStagingTableName(entityType, system);
            stagingTableManager.prepareStagingTableAndReturnStagingName(entityType, system, context, stagingTableName);
        } catch (RuntimeException e) {
            throw new RuntimeException("Unable to build stagingTable" + e);
        }
        System.out.println(stagingTableName + "GENERATED");

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

    private void streamAndPersist(File file, String tableName, String[] headers, EntitlementAssignmentProcessingContext ctx)
            throws IOException, CsvValidationException {

        int BATCH_SIZE = 500;
        final List<Map<String, String>> transformedBatch = new ArrayList<>(BATCH_SIZE);

        System.out.println("DEBUG: Entering streamAndPersist. File size: " + file.length());

        try {
            csvDataReader.streamCsv(file, headers, csvRow -> {
                try {
                    Map<String, String> transformedRow = new HashMap<>();

                    for (MappingConfigModel mapping : ctx.activeMappings()) {

                        String rawName = SqlUtils.safeColumnName(mapping.getTargetAttribute().getName());

                        EntityType type = mapping.getTargetAttribute().getEntityType();
                        String prefix = (type == EntityType.ACCOUNT) ? "account_" : "entitlement_";

                        String fullTargetKey = prefix + rawName;

                        Object calculatedValue = mappingExpressionEngine.calculateValue(mapping, csvRow);

                        transformedRow.put(fullTargetKey, calculatedValue != null ? calculatedValue.toString() : null);
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

        if (!transformedBatch.isEmpty()) {
            flush(tableName, transformedBatch, ctx);
            System.out.println("DEBUG: Final flush complete.");
        }
    }

    private void flush(String table, List<Map<String, String>> batch, EntitlementAssignmentProcessingContext ctx) {
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
