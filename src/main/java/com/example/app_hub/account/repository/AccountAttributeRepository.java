package com.example.app_hub.account.repository;

import com.example.app_hub.account.model.AccountAttributeModel;
import com.example.app_hub.common.repository.BaseEntityAttributeModelRepository;
import com.example.app_hub.system.model.SystemModel;

import java.util.List;

public interface AccountAttributeRepository extends BaseEntityAttributeModelRepository<AccountAttributeModel> {
    //Optional<AccountAttributeModel> findByName (String name);

    List<AccountAttributeModel> findBySystemOrSystemIsNull(SystemModel system);
}
