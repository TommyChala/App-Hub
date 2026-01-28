package com.example.app_hub.importing.config;

public record ResolvedEntitySchema(
        String stagingTable, String entityProductionTable, String hashTable,
        String businessKeyCol, String ownerJoinColumn, String prodIdCol, String ownerTable
) {
}
