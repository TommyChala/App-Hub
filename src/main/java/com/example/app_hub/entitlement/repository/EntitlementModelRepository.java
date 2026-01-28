package com.example.app_hub.entitlement.repository;

import com.example.app_hub.entitlement.model.EntitlementModel;
import com.example.app_hub.system.model.SystemModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntitlementModelRepository extends JpaRepository<EntitlementModel, UUID> {
    Optional<EntitlementModel> findByEntitlementId(String resourceId);
    Optional<List<EntitlementModel>> findBySystem (SystemModel system);
}
