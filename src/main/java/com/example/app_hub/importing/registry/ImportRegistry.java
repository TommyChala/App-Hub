package com.example.app_hub.importing.registry;

import com.example.app_hub.importing.strategy.ImportStrategy;
import com.example.app_hub.importing.type.ImportType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ImportRegistry {

    private final Map<ImportType, ImportStrategy> collectors = new HashMap<>();

    public ImportRegistry(List<ImportStrategy> strategies) {
        for (ImportStrategy strategy : strategies) {
            collectors.put(strategy.getType(), strategy);
        }
    }

    public ImportStrategy getStrategy(ImportType type) {
        return collectors.get(type);
    }
}
