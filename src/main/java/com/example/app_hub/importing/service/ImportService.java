package com.example.app_hub.importing.service;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.processor.EntityProcessor;
import com.example.app_hub.importing.factory.EntityProcessorFactory;
import com.example.app_hub.importing.factory.ImportFactory;
import com.example.app_hub.importing.model.ImportJobModel;
import com.example.app_hub.importing.observer.ImportSyncObserver;
import com.example.app_hub.system.model.SystemModel;
import com.example.app_hub.system.service.SystemService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class ImportService {

    private final ImportFactory importFactory;
    private final ImportJobService importJobService;
    private final Executor importExecutor;
    private final SystemService systemService;
    private final EntityProcessorFactory processorFactory;
    private final ImportSyncObserver syncObserver;

    public ImportService(
            ImportFactory importFactory,
            ImportJobService importJobService,
            @Qualifier("importExecutor") Executor importExecutor,
            SystemService systemService,
            EntityProcessorFactory processorFactory,
            ImportSyncObserver syncObserver
    ) {
        this.importFactory = importFactory;
        this.importJobService = importJobService;
        this.importExecutor = importExecutor;
        this.systemService = systemService;
        this.processorFactory = processorFactory;
        this.syncObserver = syncObserver;
    }

    private void supervise(Long jobId, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            System.err.println("Import job failed for system with id: " + jobId);
            e.printStackTrace();
            importJobService.updateJobStatus("Failed", jobId);
            throw new RuntimeException(e);
        }
    }

    public void gbuildStagingTablesFromDirectory(String directoryPath, String systemId) {

        File folder = new File(directoryPath);

        if (!folder.exists()) {
            throw new RuntimeException("Folder not found");
        }

        if (!folder.isDirectory()) {
            throw new RuntimeException("Folder is not a directory");
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (files == null || files.length == 0) {
            throw new RuntimeException("No .txt files found at " + directoryPath);
        }

        SystemModel system = systemService.findBySystemId(Long.valueOf(systemId));
        ImportJobModel newImportJob = importJobService.createNewJob(systemId);

        for (File file : files) {
            EntityType type = resolveType(file.getName());

            if (type == null) {
                System.out.println("SKIPPING: " + file.getName() + " (Does not match acc_, ent_, or asgn_)");
                continue;
            }

            importJobService.registerEntityActivity(newImportJob, type, file.getAbsolutePath());
            System.out.println("REGISTERED: " + type + " for Job ID: " + newImportJob.getJobId());

            CompletableFuture.runAsync(() -> {
                supervise(newImportJob.getJobId(), () -> {
                    try {
                        System.out.println("STARTING STAGING: " + type);
                        EntityProcessor<?> processor = processorFactory.getProcessor(type);

                        processor.process(file, system, type);

                        syncObserver.provideStatusReport(newImportJob.getJobId(), system, type, "STAGED");

                    } catch (Exception e) {
                        importJobService.updateActivityStatus(newImportJob.getJobId(), type, "FAILED");
                        throw new RuntimeException("Failed to stage " + type, e);
                    }
                });
            }, importExecutor);
        }
    }

    private EntityType resolveType(String fileName) {
        if (fileName.startsWith("acc_")) return EntityType.ACCOUNT;
        if (fileName.startsWith("ent_")) return EntityType.ENTITLEMENT;
        if (fileName.startsWith("asgn_")) return EntityType.ASSIGNMENT;
        return null;
    }

}
