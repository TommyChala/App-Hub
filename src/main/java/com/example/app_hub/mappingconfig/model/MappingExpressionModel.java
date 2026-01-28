package com.example.app_hub.mappingconfig.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "mapping_expression"
)
public class MappingExpressionModel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "mapping_config_id", nullable = false)
    private MappingConfigModel mappingConfig;

    @Column(name = "expression", nullable = false, length = 2000)
    private String expression;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MappingExpressionModel () {}

    public MappingExpressionModel(UUID id, MappingConfigModel mappingConfig, String expression, String description, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.mappingConfig = mappingConfig;
        this.expression = expression;
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MappingConfigModel getMappingConfig() {
        return mappingConfig;
    }

    public void setMappingConfig(MappingConfigModel mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
