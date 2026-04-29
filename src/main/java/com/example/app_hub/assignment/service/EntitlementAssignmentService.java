package com.example.app_hub.assignment.service;

import com.example.app_hub.account.repository.AccountRepository;
import com.example.app_hub.assignment.model.EntitlementAssignmentModel;
import com.example.app_hub.assignment.repository.EntitlementAssignmentRepository;
import com.example.app_hub.entitlement.repository.EntitlementModelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EntitlementAssignmentService {

    private final EntitlementModelRepository entitlementModelRepository;
    private final AccountRepository accountRepository;
    private final EntitlementAssignmentRepository entitlementAssignmentRepository;

    public EntitlementAssignmentService(EntitlementAssignmentRepository entitlementAssignmentRepository, AccountRepository accountRepository,
                                        EntitlementModelRepository entitlementModelRepository) {
        this.entitlementModelRepository = entitlementModelRepository;
        this.accountRepository = accountRepository;
        this.entitlementAssignmentRepository = entitlementAssignmentRepository;
        //this.entitlementAssignmentMapper = entitlementAssignmentMapper;
    }
}
