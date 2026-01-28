package com.example.app_hub.entitlement.repository;

import com.example.app_hub.common.repository.BaseEntityAttributeModelRepository;
import com.example.app_hub.entitlement.model.EntitlementAttributeModel;
import com.example.app_hub.system.model.SystemModel;

import java.util.List;

public interface EntitlementAttributeRepository extends BaseEntityAttributeModelRepository<EntitlementAttributeModel> {
    List<EntitlementAttributeModel> findBySystemOrSystemIsNull (SystemModel system);
    //Optional<EntitlementAttributeModel> findByName (String name);

}
