package com.example.app_hub.mappingconfig.controller;

import com.example.app_hub.mappingconfig.dto.AssignmentMappingConfigCreateDTO;
import com.example.app_hub.mappingconfig.dto.MappingConfigBatchDTO;
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
    public ResponseEntity<Object> createBatch(
            @RequestBody MappingConfigBatchDTO batchDto
    ) {
        mappingConfigValidator.validateMandatoryAttributes(batchDto.data(), batchDto.systemId(), batchDto.entityTypeStr());
        batchDto.data().forEach(
                mapping -> mappingConfigModelService.addNew(mapping, batchDto.systemId(), batchDto.entityTypeStr())
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/assignment")
    public List<MappingConfigModel> createAssignmentMapping (
            @RequestBody AssignmentMappingConfigCreateDTO assignmentMappingConfigCreateDTO
    )
    {
        return mappingConfigModelService.createNewAssignmentConfig(assignmentMappingConfigCreateDTO);
    }
}
