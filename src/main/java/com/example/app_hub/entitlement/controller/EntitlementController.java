package com.example.app_hub.entitlement.controller;

import com.example.app_hub.entitlement.dto.EntitlementModelCreateDTO;
import com.example.app_hub.entitlement.dto.EntitlementModelResponseDTO;
import com.example.app_hub.entitlement.service.EntitlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entitlement")
public class EntitlementController {

    private final EntitlementService entitlementService;

    public EntitlementController(EntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @GetMapping("/{systemId}")
    public ResponseEntity<List<EntitlementModelResponseDTO>> findAll(@RequestParam String systemId) {
        List<EntitlementModelResponseDTO> allResources = entitlementService.findAllBySystemId(Long.valueOf(systemId));
        return ResponseEntity.ok(allResources);

    }

    @PostMapping("/create")
    public ResponseEntity<EntitlementModelResponseDTO> create(@RequestBody EntitlementModelCreateDTO entitlementModelCreateDTO) {
        return ResponseEntity.ok(entitlementService.createNew(entitlementModelCreateDTO));
    }

}
