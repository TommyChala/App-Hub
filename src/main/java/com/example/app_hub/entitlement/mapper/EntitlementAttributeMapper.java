package com.example.app_hub.entitlement.mapper;

import com.example.app_hub.common.datatype.DataType;
import com.example.app_hub.entitlement.dto.EntitlementAttributeCreateDTO;
import com.example.app_hub.entitlement.dto.EntitlementAttributeResponseDTO;
import com.example.app_hub.entitlement.model.EntitlementAttributeModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntitlementAttributeMapper {

    default DataType mapStringToDataType (String dataType) {
        if (dataType == null) return null;
        return DataType.valueOf(dataType.toUpperCase());
    }

    @Mapping(source = "name", target = "name")
    @Mapping(source = "displayName", target = "displayName")
    @Mapping(source = "dataType", target = "dataType")
    public EntitlementAttributeModel toModel (EntitlementAttributeCreateDTO entitlementAttributeCreateDTO);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "displayName", target = "displayName")
    @Mapping(source = "dataType", target = "dataType")
    public EntitlementAttributeResponseDTO toResponseDTO (EntitlementAttributeModel entitlementAttributeModel);
}
