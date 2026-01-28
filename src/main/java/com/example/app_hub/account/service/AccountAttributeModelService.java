package com.example.app_hub.account.service;

import com.example.app_hub.account.dto.AccountAttributeModelCreateDTO;
import com.example.app_hub.account.dto.AccountAttributeModelResponseDTO;
import com.example.app_hub.account.mapper.AccountAttributeModelMapper;
import com.example.app_hub.account.model.AccountAttributeModel;
import com.example.app_hub.account.repository.AccountAttributeRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountAttributeModelService {

    private final AccountAttributeRepository accountAttributeRepository;
    private final AccountAttributeModelMapper accountAttributeModelMapper;

    public AccountAttributeModelService(AccountAttributeRepository accountAttributeRepository, AccountAttributeModelMapper accountAttributeModelMapper) {
        this.accountAttributeRepository = accountAttributeRepository;
        this.accountAttributeModelMapper = accountAttributeModelMapper;
    }

    public AccountAttributeModelResponseDTO addNew (AccountAttributeModelCreateDTO accountAttributeModelCreateDTO) {
        AccountAttributeModel accountAttribute = accountAttributeModelMapper.toAccountAttributeModel(accountAttributeModelCreateDTO);
        accountAttributeRepository.save(accountAttribute);
        return accountAttributeModelMapper.toResponseDTO(accountAttribute);
    }
}
