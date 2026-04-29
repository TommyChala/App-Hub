package com.example.app_hub.mappingconfig.controller;

import com.example.app_hub.common.api.ApiResponse;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.mappingconfig.dto.AssignmentMappingConfigCreateDTO;
import com.example.app_hub.mappingconfig.dto.MappingConfigBatchDTO;
import com.example.app_hub.mappingconfig.dto.MappingConfigBatchResponseDTO;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.mappingconfig.service.MappingConfigModelService;
import com.example.app_hub.mappingconfig.utility.MappingConfigValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mappingConfigModel")
public class MappingConfigModelController {

    private final MappingConfigModelService mappingConfigModelService;
    private final MappingConfigValidator mappingConfigValidator;

    public MappingConfigModelController (MappingConfigModelService mappingConfigModelService, MappingConfigValidator mappingConfigValidator) {
        this.mappingConfigModelService = mappingConfigModelService;
        this.mappingConfigValidator = mappingConfigValidator;
    }

    @PostMapping("/batch")
    public ApiResponse<MappingConfigBatchResponseDTO> createBatch(
            @RequestBody MappingConfigBatchDTO batchDto
    ) {
        mappingConfigValidator.validateMandatoryAttributes(batchDto.data(), batchDto.systemId(), batchDto.entityTypeStr());
        List<BaseEntityAttributeModel> newMappingConfigAttribute = batchDto.data().stream()
                        .map(mapping -> mappingConfigModelService.addNew(mapping, batchDto.systemId(), batchDto.entityTypeStr()
                        ))
                .toList();
        MappingConfigBatchResponseDTO mappingConfigBatchResponseDTO = new MappingConfigBatchResponseDTO(newMappingConfigAttribute);
        return ApiResponse.ok(mappingConfigBatchResponseDTO, "MappingConfig batch successfully created");

    }

    @PostMapping("/assignment")
    public ApiResponse<MappingConfigBatchResponseDTO> createAssignmentMapping (
            @RequestBody AssignmentMappingConfigCreateDTO assignmentMappingConfigCreateDTO
    )
    {
        List<BaseEntityAttributeModel> newMappingConfig = mappingConfigModelService.createNewAssignmentConfig(assignmentMappingConfigCreateDTO);

        MappingConfigBatchResponseDTO mappingConfigBatchResponseDTO = new MappingConfigBatchResponseDTO(newMappingConfig);
        return ApiResponse.ok(mappingConfigBatchResponseDTO, "New mappign config created successfully");
    }
}
