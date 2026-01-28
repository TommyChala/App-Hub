package com.example.app_hub.mappingconfig.model;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.mappingconfig.type.MappingConfigDataType;
import com.example.app_hub.mappingconfig.type.MappingConfigMappingType;
import com.example.app_hub.system.model.SystemModel;
import jakarta.persistence.*;
import org.hibernate.metamodel.mapping.MappingType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "mapping_config",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_mapping_config_system_target_type",
                        columnNames = {"system_id", "target_attribute_id", "entity_type"}
                )
        }
)
public class MappingConfigModel {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "system_id", nullable = false)
    private SystemModel system;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "source_attribute")
    private String sourceAttribute;

    @ManyToOne
    @JoinColumn(name = "target_attribute_id", nullable = false)
    private BaseEntityAttributeModel targetAttribute;

    @Enumerated(EnumType.STRING)
    @Column(name = "datatype", nullable = false)
    private MappingConfigDataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_type", nullable = false)
    private MappingConfigMappingType mappingType;

    @OneToMany(
            mappedBy = "mappingConfig",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<MappingExpressionModel> expressions = new ArrayList<>();

    public MappingConfigModel () {}

    // Cleaned up constructor
    public MappingConfigModel(
            UUID id,
            SystemModel system,
            EntityType entityType,
            String sourceAttribute,
            BaseEntityAttributeModel targetAttribute, // Uses Base Class
            MappingConfigDataType dataType,
            List<MappingExpressionModel> expressions,
            MappingConfigMappingType mappingType
    ) {
        this.id = id;
        this.system = system;
        this.entityType = entityType;
        this.sourceAttribute = sourceAttribute;
        this.targetAttribute = targetAttribute;
        this.dataType = dataType;
        this.expressions = expressions != null ? expressions : new ArrayList<>();
        this.mappingType = mappingType;
    }

    // --- GETTERS AND SETTERS ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public SystemModel getSystem() { return system; }
    public void setSystem(SystemModel system) { this.system = system; }

    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }

    public String getSourceAttribute() { return sourceAttribute; }
    public void setSourceAttribute(String sourceAttribute) { this.sourceAttribute = sourceAttribute; }

    // This now returns the Base type, making it flexible for any entity
    public BaseEntityAttributeModel getTargetAttribute() { return targetAttribute; }
    public void setTargetAttribute(BaseEntityAttributeModel targetAttribute) { this.targetAttribute = targetAttribute; }

    public List<MappingExpressionModel> getExpressions() { return expressions; }
    public void setExpressions(List<MappingExpressionModel> expressions) { this.expressions = expressions; }

    public MappingConfigDataType getDataType() { return dataType; }
    public void setDataType(MappingConfigDataType dataType) { this.dataType = dataType; }

    public MappingConfigMappingType getMappingType() { return mappingType; }
    public void setMappingType(MappingConfigMappingType mappingType) { this.mappingType = mappingType; }
}
