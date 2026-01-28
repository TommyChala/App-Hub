package com.example.app_hub.importing.factory;

import com.example.app_hub.importing.dto.CollectorRawCreateRequest;
import com.example.app_hub.importing.registry.ImportRegistry;
import com.example.app_hub.importing.strategy.ImportStrategy;
import org.springframework.stereotype.Component;

@Component
public class ImportFactory {

    private final ImportRegistry collectorRegistry;

    public ImportFactory (ImportRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
    }

    public ImportStrategy getStrategy (CollectorRawCreateRequest request) {

        return collectorRegistry.getStrategy(request.collectorType());
    }
}
