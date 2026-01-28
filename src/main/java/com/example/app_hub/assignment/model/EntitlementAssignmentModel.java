package com.example.app_hub.assignment.model;

import com.example.app_hub.account.model.AccountModel;
import com.example.app_hub.entitlement.model.EntitlementModel;
import com.example.app_hub.system.model.SystemModel;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "assignment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"account_id", "entitlement_id"})
        }
)
public class EntitlementAssignmentModel {

    @Id
    @GeneratedValue
    private UUID Id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "accountId", nullable = false)
    private AccountModel account;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "entitlementId", nullable = false)
    private EntitlementModel entitlement;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "systemId", nullable = false)
    private SystemModel system;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent;

    public EntitlementAssignmentModel() {}

    public EntitlementAssignmentModel(AccountModel account, EntitlementModel entitlement) {
        this.account = account;
        this.entitlement = entitlement;
    }

    public UUID getId() {
        return Id;
    }

    public void setId(UUID id) {
        Id = id;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }

    public EntitlementModel getEntitlement() {
        return entitlement;
    }

    public void setEntitlement(EntitlementModel resource) {
        this.entitlement = resource;
    }
}
