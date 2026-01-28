package com.example.app_hub.entitlement.service;

import com.example.app_hub.entitlement.dto.EntitlementAttributeCreateDTO;
import com.example.app_hub.entitlement.dto.EntitlementAttributeResponseDTO;
import com.example.app_hub.entitlement.mapper.EntitlementAttributeMapper;
import com.example.app_hub.entitlement.model.EntitlementAttributeModel;
import com.example.app_hub.entitlement.repository.EntitlementAttributeRepository;
import org.springframework.stereotype.Service;

@Service
public class EntitlementAttributeService {

    private final EntitlementAttributeRepository entitlementAttributeRepository;
    private final EntitlementAttributeMapper entitlementAttributeMapper;

    public EntitlementAttributeService (
            EntitlementAttributeRepository entitlementAttributeRepository,
            EntitlementAttributeMapper entitlementAttributeMapper
    ) {
        this.entitlementAttributeRepository = entitlementAttributeRepository;
        this.entitlementAttributeMapper = entitlementAttributeMapper;
    }

    public EntitlementAttributeResponseDTO addNew (EntitlementAttributeCreateDTO entitlementAttributeCreateDTO) {
        EntitlementAttributeModel newEntitlementAttribute =  entitlementAttributeRepository.save(entitlementAttributeMapper.toModel(entitlementAttributeCreateDTO));
        return entitlementAttributeMapper.toResponseDTO(newEntitlementAttribute);
    }
}
