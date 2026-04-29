package com.example.app_hub.importing.service;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.importing.model.ImportJobActivityModel;
import com.example.app_hub.importing.model.ImportJobModel;
import com.example.app_hub.importing.repository.ImportJobActivityModelRepository;
import com.example.app_hub.importing.repository.ImportJobModelRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ImportJobService {

    private final ImportJobModelRepository importJobModelRepository;
    private final ImportJobActivityModelRepository activityRepository;

    public ImportJobService (ImportJobModelRepository importJobModelRepository, ImportJobActivityModelRepository activityRepository) {
        this.importJobModelRepository = importJobModelRepository;
        this.activityRepository = activityRepository;
    }

    public List<EntityType> getRegisteredTypesForJob(Long jobId) {
        return activityRepository.findAllByImportJobModel_JobId(jobId)
                .stream()
                .map(ImportJobActivityModel::getType)
                .toList();
    }

    @Transactional
    public ImportJobModel createNewJob(String systemId) {
        ImportJobModel job = new ImportJobModel(systemId, "IN_PROGRESS");
        job.setStartTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        return importJobModelRepository.save(job);
    }

    @Transactional
    public void registerEntityActivity(ImportJobModel job, EntityType type, String filePath) {
        ImportJobActivityModel activity = new ImportJobActivityModel();
        activity.setImportJobModel(job);
        activity.setType(type);
        activity.setFile_path(filePath);
        activity.setStatus("PENDING");
        activityRepository.save(activity);
    }

    @Transactional
    public void updateActivityStatus(Long jobId, EntityType type, String status) {
        ImportJobModel job = importJobModelRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        ImportJobActivityModel activity = activityRepository.findByImportJobModelAndType(job, type)
                .orElseThrow(() -> new RuntimeException("Activity record missing for type: " + type));

        activity.setStatus(status);
        job.setUpdateTime(LocalDateTime.now());

        activityRepository.save(activity);
        importJobModelRepository.save(job);
    }

    public void updateJobStatus (String jobStatus, Long jobId) {
        ImportJobModel currentImportJob = importJobModelRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Unable to find import job")
                );
        currentImportJob.setJobStatus(jobStatus);
        currentImportJob.setUpdateTime(LocalDateTime.now());
        importJobModelRepository.save(currentImportJob);
    }

    public Optional<ImportJobModel> getActiveImportJob () {
        return importJobModelRepository.getActiveImportJob();
    }

    public boolean isEverythingStaged(Long jobId) {
        ImportJobModel job = importJobModelRepository.findById(jobId).orElseThrow();
        return activityRepository.countByImportJobModelAndStatusNot(job, "STAGED") == 0;
    }
    public boolean isEverythingReconciled (Long jobId) {
        ImportJobModel job = importJobModelRepository.findById(jobId).orElseThrow();
        return activityRepository.countByImportJobModelAndStatusNot(job, "RECONCILED") == 0;
    }

    @Transactional
    public void markJobCompleted(Long jobId) {
        importJobModelRepository.findById(jobId).ifPresent(job -> {
            job.setJobStatus("COMPLETED");
            job.setUpdateTime(LocalDateTime.now());
            importJobModelRepository.save(job);
        });
    }
}
