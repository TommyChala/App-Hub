package com.example.app_hub.account.service;

import com.example.app_hub.account.dto.AccountModelCreateDTO;
import com.example.app_hub.account.model.AccountModel;
import com.example.app_hub.account.repository.AccountRepository;
import com.example.app_hub.system.model.SystemModel;
import com.example.app_hub.system.repository.SystemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final SystemRepository systemRepository;

    public AccountService(AccountRepository accountRepository, SystemRepository systemRepository) {
        this.accountRepository = accountRepository;
        this.systemRepository = systemRepository;
    }

    public List<AccountModel> findAll() {
        return accountRepository.findAll();
    }

    public AccountModel createNew(AccountModelCreateDTO accountModelCreateDTO) {
        AccountModel account = new AccountModel();
        account.setAccountId(accountModelCreateDTO.accountId());
        //account.setAccountName(accountModelCreateDTO.accountName());
        SystemModel system = systemRepository.findBySystemId(accountModelCreateDTO.systemId())
                .orElseThrow(() -> new RuntimeException("Unable to create account. No system found with referenced id")
                );
        account.setSystem(system);
        return accountRepository.save(account);
    }
}
