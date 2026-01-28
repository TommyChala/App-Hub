package com.example.app_hub.importing.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class ImportJobModel {


    @Id
    @GeneratedValue
    private Long jobId;
    private String systemId;
    private String jobStatus;
    private String statusMessage;
    private LocalDateTime startTime;
    private LocalDateTime updateTime;
    @OneToMany(mappedBy = "importJobModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportJobActivityModel> jobActivity;

    public ImportJobModel () {}

    public ImportJobModel(String systemId, String jobStatus) {
        this.systemId = systemId;
        this.jobStatus = jobStatus;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public List<ImportJobActivityModel> getJobActivity() {
        return jobActivity;
    }

    public void setJobActivity(List<ImportJobActivityModel> jobActivity) {
        this.jobActivity = jobActivity;
    }
}
