package com.example.app_hub.importing.config;

public record EntityMetaData(
        String businessKeyCol,
        String ownerJoinColumn,
        String prodIdCol,
        String hashTable,
        String ownerTable
) {
}
