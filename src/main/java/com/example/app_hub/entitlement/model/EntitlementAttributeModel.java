package com.example.app_hub.entitlement.model;

import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entitlementAttributeModel")
public class EntitlementAttributeModel extends BaseEntityAttributeModel {

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntitlementAttributeValueModel> values = new ArrayList<>();

    @OneToMany(mappedBy = "targetAttribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MappingConfigModel> mappingConfigs = new ArrayList<>();

    public EntitlementAttributeModel() { super(); }

    public EntitlementAttributeModel(String name, String displayName, DataType dataType, boolean required) {
        super(name, displayName, dataType, required);
    }

    public EntityType getEntityType() {
        return EntityType.ENTITLEMENT;
    }
}
