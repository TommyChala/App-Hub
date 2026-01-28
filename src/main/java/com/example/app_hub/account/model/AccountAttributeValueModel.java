package com.example.app_hub.account.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "AccountAttributeValueModel",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "attribute_id"})
)
public class AccountAttributeValueModel {

    @GeneratedValue
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private AccountAttributeModel attribute;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountModel account;

    @Column(name = "is_row_latest", nullable = false)
    private boolean isRowLatest = true;

    @Column(name = "value_string")
    private String stringValue;

    @Column(name = "value_integer")
    private Integer intValue;

    @Column(name = "value_datetime")
    private LocalDateTime datetimeValue;

    @Column(name = "value_float")
    private Double doubleValue;

    @Column(name = "value_boolean")
    private boolean booleanValue;

    public AccountAttributeValueModel () {}

    public AccountAttributeValueModel(
            UUID id,
            AccountAttributeModel attribute,
            AccountModel account,
            boolean isRowLatest,
            String stringValue,
            Integer intValue,
            LocalDateTime datetimeValue,
            Double doubleValue,
            boolean booleanValue
    ) {
        this.id = id;
        this.attribute = attribute;
        this.account = account;
        this.isRowLatest = isRowLatest;
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.datetimeValue = datetimeValue;
        this.doubleValue = doubleValue;
        this.booleanValue = booleanValue;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AccountAttributeModel getAttribute() {
        return attribute;
    }

    public void setAttribute(AccountAttributeModel attribute) {
        this.attribute = attribute;
    }

    public AccountModel getAccount() {
        return account;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
    }

    public boolean isRowLatest() {
        return isRowLatest;
    }

    public void setRowLatest(boolean rowLatest) {
        isRowLatest = rowLatest;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public LocalDateTime getDatetimeValue() {
        return datetimeValue;
    }

    public void setDatetimeValue(LocalDateTime datetimeValue) {
        this.datetimeValue = datetimeValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}

