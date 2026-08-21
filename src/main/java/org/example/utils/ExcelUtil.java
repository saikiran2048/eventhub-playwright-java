package org.example.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    private static final Logger log = LogManager.getLogger(ExcelUtil.class);

    // Private constructor — static utility class
    private ExcelUtil() {}

    /**
     * Reads all rows from a given sheet.
     * Returns List of Maps → each Map is one row.
     * Key = column header, Value = cell value.
     *
     * Example row returned:
     * { "TestCaseName":"ValidLogin", "Username":"standard_user",
     *   "Password":"secret_sauce",  "RunMode":"Y" }
     */
    public static List<Map<String, String>> getSheetData(
            String filePath, String sheetName) {

        List<Map<String, String>> data = new ArrayList<>();

        log.info("Reading Excel → file: [{}] sheet: [{}]", filePath, sheetName);

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new RuntimeException(
                        "Sheet not found: [" + sheetName + "] in " + filePath
                );
            }

            // Row 0 → headers
            Row headerRow = sheet.getRow(0);
            int colCount  = headerRow.getLastCellNum();

            // Row 1 onwards → data
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                Map<String, String> rowMap = new LinkedHashMap<>();

                for (int colIdx = 0; colIdx < colCount; colIdx++) {
                    String header    = getCellValue(headerRow.getCell(colIdx));
                    String cellValue = getCellValue(row.getCell(colIdx));
                    rowMap.put(header, cellValue);
                }

                data.add(rowMap);
            }

            log.info("Excel read complete — {} data rows found", data.size());

        } catch (IOException e) {
            log.error("Failed to read Excel file: {}", filePath);
            throw new RuntimeException("Excel read failed: " + filePath, e);
        }

        return data;
    }

    /**
     * Filters rows where RunMode = Y only.
     * Rows with RunMode = N are silently skipped.
     */
    public static List<Map<String, String>> getRunnableRows(
            String filePath, String sheetName) {

        List<Map<String, String>> allRows     = getSheetData(filePath, sheetName);
        List<Map<String, String>> runnableRows = new ArrayList<>();

        for (Map<String, String> row : allRows) {
            String runMode = row.getOrDefault("RunMode", "N").trim();
            if (runMode.equalsIgnoreCase("Y")) {
                runnableRows.add(row);
            } else {
                log.warn("Skipping test [{}] — RunMode = N",
                        row.getOrDefault("TestCaseName", "Unknown"));
            }
        }

        log.info("Runnable rows: {} / Total rows: {}",
                runnableRows.size(), allRows.size());

        return runnableRows;
    }

    /**
     * Converts List<Map> to Object[][] format for TestNG @DataProvider.
     * Each Object[] row = { rowMap }
     */
    public static Object[][] toDataProviderFormat(
            String filePath, String sheetName) {

        List<Map<String, String>> rows = getRunnableRows(filePath, sheetName);

        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i); // each element is one Map
        }
        return data;
    }

    // ─── Private helper ────────────────────────────────────────────

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        // Force all cells to be read as String
        // Prevents issues with numeric cells (e.g. phone numbers)
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }
}
