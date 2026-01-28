package com.example.app_hub.mappingconfig.repository;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.mappingconfig.model.MappingConfigModel;
import com.example.app_hub.system.model.SystemModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MappingConfigModelRepository extends JpaRepository<MappingConfigModel, UUID> {
    @EntityGraph(attributePaths = {"expressions", "targetAttribute"})
    List<MappingConfigModel> findBySystemOrSystemIsNull(SystemModel system);

    @Query("SELECT m FROM MappingConfigModel m WHERE (m.system = :system OR m.system IS NULL) AND m.entityType = :entityType")
    List<MappingConfigModel> findBySystemAndEntityType(
            @Param("system") SystemModel system,
            @Param("entityType") EntityType entityType
    );
    List<MappingConfigModel> findByEntityTypeAndSystemOrSystemIsNull(
            EntityType entityType, SystemModel system
    );

}
