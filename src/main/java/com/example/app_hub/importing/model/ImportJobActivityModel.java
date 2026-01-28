package com.example.app_hub.importing.model;

import com.example.app_hub.common.entitytype.EntityType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "import_job_activities")
public class ImportJobActivityModel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "jobId", nullable = false)
    private ImportJobModel importJobModel;

    @Column(nullable = false)
    private EntityType type;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String file_path;

    public ImportJobActivityModel() {}

    public ImportJobActivityModel(UUID id, ImportJobModel importJobModel, EntityType type, String status, String file_path) {
        this.id = id;
        this.importJobModel = importJobModel;
        this.type = type;
        this.status = status;
        this.file_path = file_path;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ImportJobModel getImportJobModel() {
        return importJobModel;
    }

    public void setImportJobModel(ImportJobModel importJobModel) {
        this.importJobModel = importJobModel;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }
}
