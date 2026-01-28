package com.example.app_hub.importing.factory;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.common.processor.EntityProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EntityProcessorFactory {

    private final Map<EntityType, EntityProcessor<?>> processors;

    @Autowired
    public EntityProcessorFactory (Map<String, EntityProcessor<?>> processorBeans) {
        this.processors = processorBeans.values().stream()
                .collect(Collectors.toMap(
                        EntityProcessor::getType,
                        Function.identity()
                ));
    }

    /**
     * Retrieves the correct processor implementation for the given EntityType.
     * @param type The type of entity being collected (ACCOUNT, ENTITLEMENT, etc.).
     * @return The specific EntityProcessor implementation.
     * @throws IllegalArgumentException if no processor is registered for the given type.
     */
    public EntityProcessor<?> getProcessor(EntityType type) {
        EntityProcessor<?> processor = processors.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("No processor found for entity type: " + type);
        }
        return processor;
    }
}
