package com.example.app_hub.importing.observer;

import com.example.app_hub.account.processor.AccountProcessingContext;
import com.example.app_hub.assignment.processor.EntitlementAssignmentProcessingContext;
import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.processor.EntityProcessor;
import com.example.app_hub.common.processor.ProcessingContext;
import com.example.app_hub.entitlement.processor.EntitlementProcessingContext;
import com.example.app_hub.importing.config.EntitySchemaRegistry;
import com.example.app_hub.importing.config.ResolvedEntitySchema;
import com.example.app_hub.importing.factory.EntityProcessorFactory;
import com.example.app_hub.importing.factory.ProcessingContextFactory;
import com.example.app_hub.importing.service.*;
import com.example.app_hub.system.model.SystemModel;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImportSyncObserver {
    private final ImportJobService jobService;
    private final ProcessingContextFactory contextFactory;
    private final ReconciliationService reconciliationService;
    private final EntitySchemaRegistry schemaRegistry;
    private final EntityPromotionService entityPromotionService;
    private final AssignmentReconciliationService assignmentReconciliationService;
    private final AssignmentPromotionService assignmentPromotionService;

    public ImportSyncObserver(
            ImportJobService jobService,
            EntityProcessorFactory processorFactory,
            ProcessingContextFactory contextFactory,
            ReconciliationService reconciliationService,
            EntitySchemaRegistry schemaRegistry,
            EntityPromotionService entityPromotionService,
            AssignmentReconciliationService assignmentReconciliationService,
            AssignmentPromotionService assignmentPromotionService
    ) {
        this.jobService = jobService;
        this.contextFactory = contextFactory;
        this.reconciliationService = reconciliationService;
        this.schemaRegistry = schemaRegistry;
        this.entityPromotionService = entityPromotionService;
        this.assignmentReconciliationService = assignmentReconciliationService;
        this.assignmentPromotionService = assignmentPromotionService;
    }


    public synchronized void provideStatusReport(Long jobId, SystemModel system, EntityType type, String status) {

        jobService.updateActivityStatus(jobId, type, status);
        checkJobProgress(jobId, system);

    }

    private void checkJobProgress (Long jobId, SystemModel system) {
        if (jobService.isEverythingStaged(jobId)) {
            performReconciliation(jobId, system);
        }
        if (jobService.isEverythingReconciled(jobId)) {
            promoteToProduction(jobId, system);
        }
    }
    private void performReconciliation (Long jobId, SystemModel system) {

        List<EntityType> registeredTypes = jobService.getRegisteredTypesForJob(jobId);
        List<EntityType> preferredOrder = List.of(EntityType.ACCOUNT, EntityType.ENTITLEMENT, EntityType.ASSIGNMENT);
        for (EntityType type : preferredOrder) {
            if (!registeredTypes.contains(type)) {
                System.out.println("⏭️ Skipping " + type + " - File was not provided.");
                continue;
            }
            ResolvedEntitySchema schema = schemaRegistry.resolve(type, system);
            if (type == EntityType.ASSIGNMENT) {
                assignmentReconciliationService.performReconciliation(system, schema, jobId);
            }
            else if (type == EntityType.ACCOUNT || type == EntityType.ENTITLEMENT) {
                reconciliationService.performReconciliation(system, schema, jobId, type);
            }
            else {
                throw new IllegalArgumentException("Unknown type provided :" + type);
            }

            jobService.updateActivityStatus(jobId, type, "RECONCILED");
            checkJobProgress(jobId, system);

        }
    }

    private void promoteToProduction(Long jobId, SystemModel system) {
        List<EntityType> registeredTypes = jobService.getRegisteredTypesForJob(jobId);

        List<EntityType> preferredOrder = List.of(EntityType.ACCOUNT, EntityType.ENTITLEMENT, EntityType.ASSIGNMENT);

        for (EntityType type : preferredOrder) {
            if (!registeredTypes.contains(type)) {
                System.out.println("⏭️ Skipping " + type + " - File was not provided.");
                continue;
            }
            if (type == EntityType.ACCOUNT) {
                AccountProcessingContext context = contextFactory.buildAccountContext(system);
                entityPromotionService.promoteEntities(type, system, context.allAttributes());
            }
            else if (type == EntityType.ENTITLEMENT) {
                EntitlementProcessingContext context = contextFactory.buildEntitlementContext(system);
                entityPromotionService.promoteEntities(type, system, context.allAttributes());
            }
            else if (type == EntityType.ASSIGNMENT) {
                EntitlementAssignmentProcessingContext context = contextFactory.buildAssignmentContext(system);
                assignmentPromotionService.promoteAssignments(type, system);
            }
            else {
                throw new RuntimeException("Error: Entiy Type: " + type + "not recognized");
            }
            jobService.updateActivityStatus(jobId, type, "COMPLETED");
        }
        jobService.markJobCompleted(jobId);
        checkJobProgress(jobId, system);
    }


}
