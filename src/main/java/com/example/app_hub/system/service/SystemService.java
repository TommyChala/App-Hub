package com.example.app_hub.system.service;

import com.example.app_hub.system.dto.SystemResponseDTO;
import com.example.app_hub.system.model.SystemModel;
import com.example.app_hub.system.repository.SystemRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SystemService {

    private final SystemRepository systemRepository;
    private final JdbcTemplate jdbcTemplate;

    public SystemService (
            SystemRepository systemRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.systemRepository = systemRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public SystemModel findBySystemId(Long systemId) {
        return systemRepository.findBySystemId(systemId)
                .orElseThrow(() -> new RuntimeException("Cannot find system with id: " + systemId)
                );
    }

    public SystemResponseDTO registerNewSystem(SystemModel systemModel) {
        Optional<SystemModel> system = systemRepository.findByName(systemModel.getName());

        if (system.isPresent()) {
            throw new RuntimeException("Error creating system. Name is already taken: " + systemModel.getName());
        }
        SystemModel newSystem = systemRepository.save(systemModel);

      return new SystemResponseDTO(
              newSystem.getName(),
              newSystem.getSystemId()
      );
    }

    private boolean tableExists(String tableName) {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, tableName.toLowerCase()));
    }

    private void createProductionTable(String tableName) {
        String sql = String.format("""
            CREATE TABLE %s (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                system_id BIGINT NOT NULL,
                row_hash CHAR(64),
                identity_id UUID, -- For the JoinRules later
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""", tableName);
        jdbcTemplate.execute(sql);
    }
}
