package com.example.app_hub.entitlement.service;

import com.example.app_hub.entitlement.dto.EntitlementModelCreateDTO;
import com.example.app_hub.entitlement.dto.EntitlementModelResponseDTO;
import com.example.app_hub.entitlement.mapper.EntitlementModelMapper;
import com.example.app_hub.entitlement.model.EntitlementModel;
import com.example.app_hub.entitlement.repository.EntitlementModelRepository;
import com.example.app_hub.system.model.SystemModel;
import com.example.app_hub.system.repository.SystemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntitlementService {

    private final EntitlementModelRepository entitlementRepository;
    private final EntitlementModelMapper entitlementModelMapper;
    private final SystemRepository systemRepository;

    public EntitlementService(
            EntitlementModelRepository entitlementRepository,
            EntitlementModelMapper entitlementModelMapper,
            SystemRepository systemRepository
    ) {
        this.entitlementRepository = entitlementRepository;
        this.entitlementModelMapper = entitlementModelMapper;
        this.systemRepository = systemRepository;
    }

    public List<EntitlementModelResponseDTO> findAllBySystemId(Long systemId) {
        SystemModel system = systemRepository.findBySystemId(systemId)
                .orElseThrow(() -> new RuntimeException("system not found")
                );
        List<EntitlementModel> allEntitlements = entitlementRepository.findBySystem(system)
                .orElseThrow(() -> new RuntimeException("entitlements not found")
                );

        return allEntitlements.stream()
                .map(entitlementModelMapper::toResponseDTO)
                .toList();
    }

    public EntitlementModelResponseDTO createNew(EntitlementModelCreateDTO entitlementModelCreateDTO) {
        EntitlementModel entitlementModel = entitlementModelMapper.toModel(entitlementModelCreateDTO);

        entitlementRepository.save(entitlementModel);
        return entitlementModelMapper.toResponseDTO(entitlementModel);
    }
}
