package com.example.app_hub.common.repository;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.model.BaseEntityAttributeModel;
import com.example.app_hub.system.model.SystemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseEntityAttributeModelRepository <T extends BaseEntityAttributeModel> extends JpaRepository<T, UUID> {
    List<T> findBySystemOrSystemIsNull(SystemModel system);
    //List<T> findByEntityTypeAndSystemOrSystemIsNull(EntityType entityType, SystemModel system);
    Optional<BaseEntityAttributeModel> findByName (String name);
    Optional<BaseEntityAttributeModel> findByNameAndEntityType (String name, EntityType entityType);
}
