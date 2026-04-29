package com.example.app_hub.importing.observer;

import com.example.app_hub.common.entitytype.EntityType;
import com.example.app_hub.importing.config.EntitySchemaRegistry;
import com.example.app_hub.importing.observer.ImportSyncObserver;
import com.example.app_hub.importing.service.ImportJobService;
import com.example.app_hub.importing.service.ReconciliationService;
import com.example.app_hub.system.model.SystemModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class ImportSyncObserverTest {

    @Mock
    private ImportJobService jobService;

    @Mock
    private ReconciliationService reconciliationService;

    @Mock
    private EntitySchemaRegistry schemaRegistry;

    @InjectMocks
    private ImportSyncObserver observer;

    @Test
    void provideStatusReport_shouldTriggerReconciliation_whenEverythingIsStaged() {
        // Arrange
        Long jobId = 1L;
        SystemModel system = new SystemModel();
        EntityType type = EntityType.ACCOUNT;

        // Tell the mock what to return
        when(jobService.isEverythingStaged(jobId)).thenReturn(true, false);
        when(jobService.isEverythingReconciled(jobId)).thenReturn(false, true);
        when(jobService.getRegisteredTypesForJob(jobId)).thenReturn(List.of(EntityType.ACCOUNT));

        // Act
        observer.provideStatusReport(jobId, system, type, "STAGED");

        // Assert
        verify(reconciliationService).performReconciliation(eq(system), any(), eq(jobId), eq(EntityType.ACCOUNT));
        verify(jobService).updateActivityStatus(jobId, EntityType.ACCOUNT, "RECONCILED");
    }
}