package com.example.app_hub.mappingconfig.mapper;

import com.example.app_hub.mappingconfig.dto.MappingConfigModelCreateDTO;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.mappingconfig.type.MappingConfigDataType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MappingConfigModelMapper {

    default MappingConfigDataType mapStringToDataType(String dataType) {
        if (dataType == null) return null;
        return MappingConfigDataType.valueOf(dataType.toUpperCase()); // case-insensitive
    }

    @Mapping(source = "sourceAttribute", target = "sourceAttribute")
    @Mapping(target = "targetAttribute", ignore = true)
    @Mapping(source = "dataType", target = "dataType")
    @Mapping(source = "mappingType", target = "mappingType")
    public MappingConfigModel toModel (MappingConfigModelCreateDTO mappingConfigModelCreateDTO);
}
