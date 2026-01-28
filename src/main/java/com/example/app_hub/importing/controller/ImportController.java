package com.example.app_hub.importing.controller;

import com.example.app_hub.importing.dto.BatchImportRequest;
import com.example.app_hub.importing.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/imports")
public class ImportController {

    private final ImportService importService;


    public ImportController (ImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<String> batchImport (@RequestBody BatchImportRequest request) {

        importService.buildStagingTablesFromDirectory(request.directoryPath(), request.systemId());
        return ResponseEntity.ok("Import started");
    }

}
