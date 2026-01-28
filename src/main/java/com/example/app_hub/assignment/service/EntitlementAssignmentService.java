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
    //private final EntitlementAssignmentMapper entitlementAssignmentMapper;

    public EntitlementAssignmentService(EntitlementAssignmentRepository entitlementAssignmentRepository, AccountRepository accountRepository,
                                        EntitlementModelRepository entitlementModelRepository) {
        this.entitlementModelRepository = entitlementModelRepository;
        this.accountRepository = accountRepository;
        this.entitlementAssignmentRepository = entitlementAssignmentRepository;
        //this.entitlementAssignmentMapper = entitlementAssignmentMapper;
    }
/*
    public EntitlementAssignmentModel createEntitlementAssignment (EntitlementAssignmentCreateDTO createRequest) {
        EntitlementAssignmentModel newResourceAssignment = entitlementAssignmentMapper.CreateDTOToIdentity(createRequest);
        newResourceAssignment.setEntitlement(iEntitlementRepository.findByEntitlementId(createRequest.getEntitlementId())
                .orElseThrow(() -> new EntityNotFoundException("Could not find resource by id: " + createRequest.getEntitlementId())
                )
        );
        newResourceAssignment.setAccount(iAccountRepository.findByAccountId(createRequest.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Could not find account by id: " + createRequest.getAccountId())
                )
        );
        return entitlementAssignmentRepository.save(newResourceAssignment);
    }

 */

}
