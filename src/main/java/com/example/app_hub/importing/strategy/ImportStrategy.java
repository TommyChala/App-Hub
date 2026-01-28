package com.example.app_hub.importing.strategy;

import com.example.app_hub.importing.dto.CollectorRawCreateRequest;
import com.example.app_hub.importing.type.ImportType;

public interface ImportStrategy {
    ImportType getType();

    void collect(CollectorRawCreateRequest source);
}
