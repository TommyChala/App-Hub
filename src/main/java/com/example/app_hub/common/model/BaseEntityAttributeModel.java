package com.example.app_hub.common.model;

import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.system.model.SystemModel;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "base_entity_attributes")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseEntityAttributeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;

    @Column(name = "required", nullable = false)
    private boolean required;

    @ManyToOne
    @JoinColumn(name = "system_id")
    private SystemModel system;

    protected BaseEntityAttributeModel() {}

    // Shared Constructor
    public BaseEntityAttributeModel(String name, String displayName, DataType dataType, boolean required) {
        this.name = name;
        this.displayName = displayName;
        this.dataType = dataType;
        this.required = required;
    }

    public abstract EntityType getEntityType();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public SystemModel getSystem() { return system; }
    public void setSystem(SystemModel system) { this.system = system; }
}
