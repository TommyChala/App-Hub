package com.example.app_hub.account.mapper;

import com.example.app_hub.account.dto.AccountModelCreateDTO;
import com.example.app_hub.account.dto.AccountModelResponseDTO;
import com.example.app_hub.account.model.AccountModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    //@Mapping(source = "accountName", target = "accountName")
    @Mapping(source = "accountId", target = "accountId")
    public AccountModelResponseDTO toResponseDTO (AccountModel accountModel);

    @Mapping(source = "accountId", target = "accountId")
    //@Mapping(source = "accountName", target = "accountName")
    public AccountModel toAccountModel (AccountModelCreateDTO accountModelCreateDTO);
}
