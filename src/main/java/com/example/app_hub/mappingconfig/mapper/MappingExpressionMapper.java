package com.example.app_hub.mappingconfig.mapper;

import com.example.app_hub.mappingconfig.dto.MappingExpressionCreateDTO;
import com.example.app_hub.mappingconfig.model.MappingExpressionModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MappingExpressionMapper {

    @Mapping(source = "expression", target = "expression")
    @Mapping(source = "description", target = "description")

    public MappingExpressionModel toModel (MappingExpressionCreateDTO mappingExpressionCreateDTO);
}
