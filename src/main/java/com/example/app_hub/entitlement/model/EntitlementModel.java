package com.example.app_hub.entitlement.model;

import com.example.app_hub.assignment.model.EntitlementAssignmentModel;
import com.example.app_hub.system.model.SystemModel;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "Entitlement",
        uniqueConstraints = @UniqueConstraint(columnNames = "entitlementId")
)
public class EntitlementModel {

    @Id
    @GeneratedValue
    @Column(name = "uid", nullable = false, updatable = false, unique = true)
    private UUID uid;

    @Column(name = "entitlementId", nullable = false)
    private String entitlementId;

    @ManyToOne
    @JoinColumn(name = "systemId", nullable = false)
    private SystemModel system;

    @OneToMany(mappedBy = "entitlement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntitlementAssignmentModel> assignment;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent;

    public EntitlementModel() {}

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public String getEntitlementId() {
        return entitlementId;
    }

    public void setEntitlementId(String entitlementId) {
        this.entitlementId = entitlementId;
    }

    public SystemModel getSystem() {
        return system;
    }

    public void setSystem(SystemModel system) {
        this.system = system;
    }

    public List<EntitlementAssignmentModel> getAssignment() {
        return assignment;
    }

    public void setAssignment(List<EntitlementAssignmentModel> assignment) {
        this.assignment = assignment;
    }
}
