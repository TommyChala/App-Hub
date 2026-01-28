package com.example.app_hub.account.model;

import com.example.app_hub.system.model.SystemModel;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "account",
        uniqueConstraints = @UniqueConstraint(columnNames = "accountId")
)
public class AccountModel {

    @Id
    @Column(name = "uid", nullable = false, updatable = false)
    @GeneratedValue
    private UUID uid;

    @Column(name = "accountId", nullable = false)
    private String accountId;

    @ManyToOne
    @JoinColumn(name = "systemId")
    private SystemModel system;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountAttributeValueModel> values = new ArrayList<>();

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent;

    public AccountModel() {}

    public AccountModel(UUID Uid, String AccountId, SystemModel system) {
        this.uid = Uid;
        this.accountId = AccountId;
        this.system = system;
    }

    public UUID getUid() {
        return uid;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public SystemModel getSystem () {
        return system;
    }

    public void setSystem (SystemModel system) {
        this.system = system;
    }

    public List<AccountAttributeValueModel> getValues() {
        return values;
    }

    public void setValues(List<AccountAttributeValueModel> values) {
        this.values = values;
    }

}
