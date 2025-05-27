package com.automator.model.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelReader {

    private List<Map<String, String>> rows = new ArrayList<>();
    private final DataFormatter formatter = new DataFormatter();
    
     
    // Legge il file Excel e carica tutte le righe come lista di mappe.
    public void read(File file) throws IOException {
        rows.clear();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().endsWith(".xlsx") ?
                     new XSSFWorkbook(fis) :
                     new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            List<String> headers = new ArrayList<>();

            // Lettura header con gestione intestazioni vuote
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                int colIndex = 0;
                for (Cell cell : headerRow) {
                    String header = cell != null ? formatter.formatCellValue(cell).trim() : "";
                    if (header.isEmpty()) {
                        header = "Column" + colIndex;
                    }
                    headers.add(header);
                    colIndex++;
                }
            }

            // Lettura righe dati
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowData = new LinkedHashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    String value = formatter.formatCellValue(cell).trim();
                    if (!value.isEmpty()) {
                        rowData.put(headers.get(i), value);
                    }
                }

                rows.add(rowData);
            }
        }
    }

    
     //Ritorna la prima riga dove la colonna `columnName` ha valore `value`.
     
    public Map<String, String> getRowByColumnValue(String columnName, String value) {
        for (Map<String, String> row : rows) {
            if (value.equalsIgnoreCase(row.get(columnName))) {
                return row;
            }
        }
        return null; 
    }

    
     //Dato una riga e il nome di una colonna, ritorna il valore.
     
    public String getCellValueFromRow(Map<String, String> row, String columnName) {
        return row != null ? row.getOrDefault(columnName, "") : "";
    }

    
     //Ritorna tutte le righe lette .
     
    public List<Map<String, String>> getAllRows() {
        return rows;
    }

    
     //Utility privata per convertire una cella in stringa.
     
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell);
    }

}
