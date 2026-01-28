package com.example.app_hub.entitlement.controller;

import com.example.app_hub.entitlement.dto.EntitlementAttributeCreateDTO;
import com.example.app_hub.entitlement.dto.EntitlementAttributeResponseDTO;
import com.example.app_hub.entitlement.service.EntitlementAttributeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/entitlement/attribute")
public class EntitlementAttributeController {

    private final EntitlementAttributeService entitlementAttributeService;

    public EntitlementAttributeController (EntitlementAttributeService entitlementAttributeService) {
        this.entitlementAttributeService = entitlementAttributeService;
    }

    @PostMapping("/create")
    public EntitlementAttributeResponseDTO addNew (@RequestBody EntitlementAttributeCreateDTO entitlementAttributeCreateDTO) {
        return entitlementAttributeService.addNew(entitlementAttributeCreateDTO);
    }
}
