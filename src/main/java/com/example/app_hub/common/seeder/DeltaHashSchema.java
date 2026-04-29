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

            jdbcTemplate.execute("""
                        CREATE INDEX IF NOT EXISTS idx_hash_lookup
                        ON account_production_hashes (system_id, businesskey)
                    """);
// 1. Create the Assignment Hash table
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS assignment_production_hashes (
                    account_bk VARCHAR(255) NOT NULL,
                    entitlement_bk VARCHAR(255) NOT NULL,
                    system_id BIGINT NOT NULL,
                    row_hash CHAR(64) NOT NULL,
                    PRIMARY KEY (account_bk, entitlement_bk, system_id)
                )
            """);

            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_assignment_hash_lookup
                ON assignment_production_hashes (system_id, account_bk, entitlement_bk)
            """);
        };


    }
}

