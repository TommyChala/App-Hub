package com.example.app_hub.importing.dto;

public record BatchImportRequest(
        String directoryPath,
        String systemId
) {
}
