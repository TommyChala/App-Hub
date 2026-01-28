package com.example.app_hub.importing.repository;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.importing.model.ImportJobActivityModel;
import com.example.app_hub.importing.model.ImportJobModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImportJobActivityModelRepository extends JpaRepository<ImportJobActivityModel, UUID> {
    Optional<ImportJobActivityModel> findByImportJobModelAndType(ImportJobModel job, EntityType type);

    long countByImportJobModelAndStatusNot(ImportJobModel job, String status);

    List<ImportJobActivityModel> findAllByImportJobModel_JobId(Long jobId);
}
