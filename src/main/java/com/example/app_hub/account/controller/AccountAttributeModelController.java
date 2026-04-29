package com.example.app_hub.account.controller;

import com.example.app_hub.account.dto.AccountAttributeModelCreateDTO;
import com.example.app_hub.account.dto.AccountAttributeModelResponseDTO;
import com.example.app_hub.account.service.AccountAttributeModelService;
import com.example.app_hub.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/account/attribute")
public class AccountAttributeModelController {

    private final AccountAttributeModelService accountAttributeModelService;

    public AccountAttributeModelController (AccountAttributeModelService accountAttributeModelService) {
        this.accountAttributeModelService = accountAttributeModelService;
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<AccountAttributeModelResponseDTO>> addAccountAttribute (@RequestBody AccountAttributeModelCreateDTO accountAttributeModelCreateDTO) {
        AccountAttributeModelResponseDTO accountAttributeModelResponseDTO = accountAttributeModelService.addNew(accountAttributeModelCreateDTO);

        ApiResponse<AccountAttributeModelResponseDTO> response = ApiResponse.ok(
                accountAttributeModelResponseDTO,
                "Account attribute " + accountAttributeModelResponseDTO.displayName() + " created successfully"
        );

        return ResponseEntity.ok(response);
    }
}