package com.example.app_hub.importing.config;

public record EntityMetaData(
        String businessKeyCol,
        String ownerJoinColumn,
        String prodIdCol,
        String hashTable, // This one is usually shared/static
        String ownerTable
) {
}
