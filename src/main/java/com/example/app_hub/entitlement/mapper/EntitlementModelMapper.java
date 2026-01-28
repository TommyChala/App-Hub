package com.example.app_hub.entitlement.mapper;

import com.example.app_hub.entitlement.dto.EntitlementModelCreateDTO;
import com.example.app_hub.entitlement.dto.EntitlementModelResponseDTO;
import com.example.app_hub.entitlement.model.EntitlementModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntitlementModelMapper {

    @Mapping(source = "entitlementId", target = "entitlementId")
    public EntitlementModel toModel (EntitlementModelCreateDTO entitlementModelCreateDTO);

    @Mapping(source = "uid", target = "uid")
    @Mapping(source = "entitlementId", target = "entitlementId")
    @Mapping(source = "system.name", target = "systemName")
    public EntitlementModelResponseDTO toResponseDTO (EntitlementModel entitlementModel);
}
