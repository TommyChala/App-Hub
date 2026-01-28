package com.example.app_hub.system.model;


import com.example.app_hub.account.model.AccountAttributeModel;
import com.example.app_hub.account.model.AccountModel;
import com.example.app_hub.assignment.model.EntitlementAssignmentModel;
import com.example.app_hub.entitlement.model.EntitlementModel;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.system.type.SystemType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "systems")
public class SystemModel {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "systemId", nullable = false, unique = true)
    private Long systemId;

    @Column(name = "systemName", nullable = false)
    private String name;

    @Column(name = "systemType")
    private SystemType type;

    @OneToMany(mappedBy = "system", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountModel> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "system", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntitlementModel> entitlements = new ArrayList<>();

    @OneToMany(mappedBy = "system", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntitlementAssignmentModel> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "system", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MappingConfigModel> mappingConfigModels = new ArrayList<>();

    @OneToMany(mappedBy = "system", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountAttributeModel> accountAttributeModels = new ArrayList<>();

    public SystemModel () {}

    public SystemModel(UUID id, String name, SystemType type, Long systemId) {
        this.id = id;
        this.name = name;
        this.systemId = systemId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SystemType getType() {
        return type;
    }

    public void setType(SystemType type) {
        this.type = type;
    }

    public List<AccountModel> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountModel> accounts) {
        this.accounts = accounts;
    }

    public List<EntitlementModel> getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(List<EntitlementModel> entitlements) {
        this.entitlements = entitlements;
    }

    public List<EntitlementAssignmentModel> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<EntitlementAssignmentModel> assignments) {
        this.assignments = assignments;
    }

    public List<MappingConfigModel> getMappingConfigModels() {
        return mappingConfigModels;
    }

    public void setMappingConfigModels(List<MappingConfigModel> mappingConfigModels) {
        this.mappingConfigModels = mappingConfigModels;
    }

    public List<AccountAttributeModel> getAccountAttributeModels() {
        return accountAttributeModels;
    }

    public void setAccountAttributeModels(List<AccountAttributeModel> accountAttributeModels) {
        this.accountAttributeModels = accountAttributeModels;
    }
}
