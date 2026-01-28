package com.example.app_hub.assignment.controller;

import com.example.app_hub.assignment.service.EntitlementAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resource-assignment")
public class EntitlementAssignmentController {

    private final EntitlementAssignmentService entitlementAssignmentService;

    public EntitlementAssignmentController(EntitlementAssignmentService entitlementAssignmentService) {
        this.entitlementAssignmentService = entitlementAssignmentService;
    }

    /*
    @PostMapping
    public ResponseEntity<Void> createResourceAssignment (@RequestBody EntitlementAssignmentCreateDTO createRequest) {
        entitlementAssignmentService.createEntitlementAssignment(createRequest);

        return ResponseEntity.noContent().build();
    }

     */
}
