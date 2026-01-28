package com.example.app_hub.entitlement.dto;

import java.util.UUID;

public record EntitlementModelResponseDTO(
        UUID uid,
        String entitlementId,
        String systemName
) {
}
