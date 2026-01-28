package com.example.app_hub.system.controller;

import com.example.app_hub.common.api.ApiResponse;
import com.example.app_hub.system.dto.SystemResponseDTO;
import com.example.app_hub.system.model.SystemModel;
import com.example.app_hub.system.service.SystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController (SystemService systemService) {
        this.systemService = systemService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SystemResponseDTO>> addNew (@RequestBody SystemModel systemModel) {
        SystemResponseDTO newSystem = systemService.registerNewSystem(systemModel);

        ApiResponse<SystemResponseDTO> response = ApiResponse.ok(
                newSystem, "System " + newSystem.systemId() + "created successfully"
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newSystem.systemId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
