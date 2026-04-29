package com.example.app_hub.common.utility;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import com.opencsv.CSVReader;

@Component
public class CsvDataReader {

    public String[] readHeaders(File file) throws IOException, CsvValidationException {
        Objects.requireNonNull(file, "File cannot be null");
        
        try (Reader reader = new FileReader(file);
             CSVReader csvReader = new CSVReader(reader)) {

            String[] rawHeaders = csvReader.readNext();
            if (rawHeaders == null) {
                throw new CsvValidationException("CSV file is empty: " + file.getName());
            }
            return CSVFileValidator.getCleanedUpHeaders(rawHeaders);
        }
    }
    public void streamCsv(
            File file,
            String[] cleanedHeaders,
            Consumer<Map<String, String>> rowConsumer
    ) throws IOException, CsvValidationException {

        try (Reader reader = new FileReader(file);
             CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext();

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < cleanedHeaders.length; i++) {
                    String value = (i < line.length) ? line[i] : null;
                    row.put(cleanedHeaders[i].toLowerCase(), value);
                }
                rowConsumer.accept(row);
            }
        }
    }
}
