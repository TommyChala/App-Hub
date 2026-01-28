package com.example.app_hub.importing.repository;

import com.example.app_hub.importing.model.ImportJobModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ImportJobModelRepository extends JpaRepository<ImportJobModel, Long> {
    @Query("SELECT i FROM ImportJobModel i WHERE i.jobStatus = 'Active'")
    Optional<ImportJobModel> getActiveImportJob();
}
