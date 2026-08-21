package org.example.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final Logger log = LogManager.getLogger(JsonUtil.class);

    // ObjectMapper is thread-safe when shared — reuse it
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonUtil() {}

    /**
     * Reads JSON array file → List of Maps.
     * Same structure as ExcelUtil.getSheetData()
     * so tests can swap data sources freely.
     */
    public static List<Map<String, String>> getJsonData(String filePath) {
        log.info("Reading JSON → file: [{}]", filePath);

        try {
            List<Map<String, String>> data = mapper.readValue(
                    new File(filePath),
                    new TypeReference<List<Map<String, String>>>() {}
            );
            log.info("JSON read complete — {} records found", data.size());
            return data;

        } catch (IOException e) {
            log.error("Failed to read JSON file: {}", filePath);
            throw new RuntimeException("JSON read failed: " + filePath, e);
        }
    }

    /**
     * Filters records where runMode = Y only.
     * Mirrors ExcelUtil.getRunnableRows() — consistent API.
     */
    public static List<Map<String, String>> getRunnableRecords(String filePath) {
        List<Map<String, String>> all      = getJsonData(filePath);
        List<Map<String, String>> runnable = new ArrayList<>();

        for (Map<String, String> record : all) {
            String runMode = record.getOrDefault("runMode", "N").trim();
            if (runMode.equalsIgnoreCase("Y")) {
                runnable.add(record);
            } else {
                log.warn("Skipping test [{}] — runMode = N",
                        record.getOrDefault("testCaseName", "Unknown"));
            }
        }

        log.info("Runnable records: {} / Total: {}",
                runnable.size(), all.size());

        return runnable;
    }

    /**
     * Converts to TestNG @DataProvider format.
     * Mirrors ExcelUtil.toDataProviderFormat() — same pattern.
     */
    public static Object[][] toDataProviderFormat(String filePath) {
        List<Map<String, String>> records = getRunnableRecords(filePath);

        Object[][] data = new Object[records.size()][1];
        for (int i = 0; i < records.size(); i++) {
            data[i][0] = records.get(i);
        }
        return data;
    }
}
