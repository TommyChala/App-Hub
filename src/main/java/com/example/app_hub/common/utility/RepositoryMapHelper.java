package com.example.app_hub.common.utility;

import com.example.app_hub.account.repository.AccountAttributeRepository;
import com.example.app_hub.account.repository.AccountRepository;
import com.example.app_hub.assignment.repository.EntitlementAssignmentRepository;
import com.example.app_hub.entitlement.model.EntitlementModel;
import com.example.app_hub.entitlement.repository.EntitlementAttributeRepository;
import com.example.app_hub.entitlement.repository.EntitlementModelRepository;
import com.example.app_hub.mappingconfig.repository.MappingConfigModelRepository;
import com.example.app_hub.system.repository.SystemRepository;
import org.springframework.stereotype.Component;

@Component
public class RepositoryMapHelper {

    private final AccountRepository accountRepository;
    private final EntitlementModelRepository resourceRepository;
    private final SystemRepository systemRepository;
    private final AccountAttributeRepository accountAttributeRepository;
    private final EntitlementAttributeRepository entitlementAttributeRepository;
    private final MappingConfigModelRepository mappingConfigModelRepository;
    private final EntitlementAssignmentRepository entitlementAssignmentRepository;

    public RepositoryMapHelper (AccountRepository accountRepository, AccountAttributeRepository accountAttributeRepository,
                                EntitlementModelRepository resourceRepository, SystemRepository systemRepository,
                                MappingConfigModelRepository mappingConfigModelRepository,
                                EntitlementAttributeRepository entitlementAttributeRepository,
                                EntitlementAssignmentRepository entitlementAssignmentRepository) {
        this.accountRepository = accountRepository;
        this.accountAttributeRepository = accountAttributeRepository;
        this.resourceRepository = resourceRepository;
        this.systemRepository = systemRepository;
        this.mappingConfigModelRepository = mappingConfigModelRepository;
        this.entitlementAttributeRepository = entitlementAttributeRepository;
        this.entitlementAssignmentRepository = entitlementAssignmentRepository;
    }

    public AccountRepository getAccountRepository() {
        return accountRepository;
    }

    public AccountAttributeRepository getAccountAttributeRepository() {
        return accountAttributeRepository;
    }

    public MappingConfigModelRepository getMappingConfigModelRepository() {
        return mappingConfigModelRepository;
    }

    public EntitlementModelRepository getResourceRepository() {
        return resourceRepository;
    }

    public SystemRepository getSystemRepository() {
        return systemRepository;
    }

    public EntitlementAttributeRepository getEntitlementAttributeRepository() {
        return entitlementAttributeRepository;
    }

    public EntitlementAssignmentRepository getEntitlementAssignmentRepository() {
        return entitlementAssignmentRepository;
    }
}
