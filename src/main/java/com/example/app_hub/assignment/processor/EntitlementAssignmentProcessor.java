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

        //STAGING VIRKER!!! Lav reconciliation fra staging til production specifikt til assignments.
        //LAV RESTEN AF ENTITLEMENTASSIGNMENT PROCESSOR. NU ER CONTEXT DANNET.
        /* Mapping skal kun bestå af accountreference (businesskey) og entitlementreference
        (businesskey). Så laver vi et lookup på businesskey i entitybase tabellen for hver
        reference baseret på entity typen. Så finder vi en række for både account og entitlement.
        Så tager vi UUID og bruger som reference for hver af dem, som giver os koblingen.
        Eks: Vi slår businesskey op sammen med entity type account i base tabellen.
        Vi finder et match på værdien, og kan tage UUID. Så gør vi det samme for entitlement.
        Måske har vi brug for indeks på businesskey så?
        Skal have sin egen persisting logik. Kan... forhåbentlig laves simpel?
         */
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

    /*

    @Override
    public void reconcile (EntityType type, SystemModel system, Long jobId) {}

    @Override
    public void promote(EntityType entityType, SystemModel system, EntitlementAssignmentProcessingContext context) {
        //reconciliationService.promoteToProduction(entityType, system, context.allAttributes());
        System.out.println(">>> [PROCESSOR SUCCESS] System: " + system.getName());
    }

     */

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
                        // 1. Get the raw target attribute name (e.g., "businesskey")
                        String rawName = SqlUtils.safeColumnName(mapping.getTargetAttribute().getName());

                        // 2. Identify the prefix based on the EntityType
                        EntityType type = mapping.getTargetAttribute().getEntityType();
                        String prefix = (type == EntityType.ACCOUNT) ? "account_" : "entitlement_";

                        // 3. This matches the column name in the Staging Table
                        String fullTargetKey = prefix + rawName;

                        // 4. Calculate the value from the CSV
                        Object calculatedValue = mappingExpressionEngine.calculateValue(mapping, csvRow);

                        // 5. Store it in the map with the FULL prefixed key
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
