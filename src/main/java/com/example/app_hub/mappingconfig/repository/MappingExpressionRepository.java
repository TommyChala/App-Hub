package com.example.app_hub.mappingconfig.repository;

import com.example.app_hub.mappingconfig.model.MappingExpressionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MappingExpressionRepository extends JpaRepository<MappingExpressionModel, UUID> {
}
