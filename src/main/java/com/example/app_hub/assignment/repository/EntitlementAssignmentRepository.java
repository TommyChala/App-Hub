package com.example.app_hub.assignment.repository;

import com.example.app_hub.assignment.model.EntitlementAssignmentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EntitlementAssignmentRepository extends JpaRepository<EntitlementAssignmentModel, UUID> {
}
