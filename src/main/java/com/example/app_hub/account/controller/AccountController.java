package com.example.app_hub.account.controller;

import com.example.app_hub.account.dto.AccountModelCreateDTO;
import com.example.app_hub.account.model.AccountModel;
import com.example.app_hub.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController (AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountModel>> findAll() {
        return ResponseEntity.ok(accountService.findAll());
    }

    @PostMapping
    public ResponseEntity<AccountModel> createNew(@RequestBody AccountModelCreateDTO accountModelCreateDTO) {
        return ResponseEntity.ok(accountService.createNew(accountModelCreateDTO));
    }
}
