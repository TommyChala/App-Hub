package com.example.app_hub.common.processor;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.system.model.SystemModel;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.IOException;

public interface EntityProcessor<C extends ProcessingContext> {

    EntityType getType();

    void process (File file, SystemModel system, EntityType entityType) throws IOException, CsvValidationException;

    /*
    void reconcile(EntityType type, SystemModel system, Long jobId);

    void promote(EntityType entityType, SystemModel system, C context);

     */
}
