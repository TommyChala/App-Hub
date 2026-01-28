package com.example.app_hub.common.seeder;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DeltaHashSchema {
    @Bean
    public ApplicationRunner initializeSchema(JdbcTemplate jdbcTemplate) {
        return args -> {
            // This creates the shared 'Memory' table for all systems
            jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS account_production_hashes (
                            businesskey VARCHAR(255) NOT NULL,
                            system_id BIGINT NOT NULL,
                            row_hash CHAR(64) NOT NULL,
                            PRIMARY KEY (businesskey, system_id)
                        )
                    """);

            // Optional: Add an index for even faster reconciliation lookups
            jdbcTemplate.execute("""
                        CREATE INDEX IF NOT EXISTS idx_hash_lookup
                        ON account_production_hashes (system_id, businesskey)
                    """);
            jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS entitlement_production_hashes (
                            businesskey VARCHAR(255) NOT NULL,
                            system_id BIGINT NOT NULL,
                            row_hash CHAR(64) NOT NULL,
                            PRIMARY KEY (businesskey, system_id)
                        )
                    """);

            // Optional: Add an index for even faster reconciliation lookups
            jdbcTemplate.execute("""
                        CREATE INDEX IF NOT EXISTS idx_hash_lookup
                        ON account_production_hashes (system_id, businesskey)
                    """);
// 1. Create the Assignment Hash table
            jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS assignment_production_hashes (
                            businesskey VARCHAR(512) NOT NULL,
                            system_id BIGINT NOT NULL,
                            row_hash CHAR(64) NOT NULL,
                            PRIMARY KEY (businesskey, system_id)
                            )
""");
//status VARCHAR(50) DEFAULT 'STAGED',
// 2. Add the index for fast reconciliation lookups
            jdbcTemplate.execute("""
                        CREATE INDEX IF NOT EXISTS idx_assignment_hash_lookup
                        ON assignment_production_hashes (system_id, businesskey)
        """);

        };


    }
}

