package com.example.app_hub.system.repository;

import com.example.app_hub.system.model.SystemModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SystemRepository extends JpaRepository<SystemModel, UUID> {
    Optional<SystemModel> findBySystemId (Long systemId);
    Optional<SystemModel> findByName (String name);
}
