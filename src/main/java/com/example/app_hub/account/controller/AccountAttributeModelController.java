package com.example.app_hub.account.controller;

import com.example.app_hub.account.dto.AccountAttributeModelCreateDTO;
import com.example.app_hub.account.dto.AccountAttributeModelResponseDTO;
import com.example.app_hub.account.service.AccountAttributeModelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account/attribute")
public class AccountAttributeModelController {

    private final AccountAttributeModelService accountAttributeModelService;

    public AccountAttributeModelController (AccountAttributeModelService accountAttributeModelService) {
        this.accountAttributeModelService = accountAttributeModelService;
    }

    @PostMapping()
    public AccountAttributeModelResponseDTO addAccountAttribute (@RequestBody AccountAttributeModelCreateDTO accountAttributeModelCreateDTO) {
        return accountAttributeModelService.addNew(accountAttributeModelCreateDTO);
    }
}