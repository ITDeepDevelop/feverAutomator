package com.automator.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class ExcelTableView extends VBox {
    private final TableView<ObservableList<String>> table;
    private File currentFile;

    public ExcelTableView() {
        table = new TableView<>();
        
        // Stile generale della tabella
        table.setStyle("-fx-background-color: white; -fx-table-cell-border-color: #e0e0e0;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Impostazioni per migliorare la visualizzazione
        table.setPadding(new Insets(10));
        table.setStyle("-fx-background-color: white; " +
                      "-fx-table-cell-border-color: #e0e0e0; " +
                      "-fx-selection-bar: #3498db; " +
                      "-fx-selection-bar-non-focused: #bdc3c7;");
        
        // Stile per le righe alternate
        table.setStyle(table.getStyle() + 
                      "-fx-table-cell-border-color: transparent transparent #e0e0e0 transparent; " +
                      "-fx-table-header-border-color: #34495e;");
        
        this.getChildren().add(table);
        this.setStyle("-fx-padding: 20; -fx-background-color: #f8f9fa; -fx-background-radius: 10;");
    }

    public void loadExcelFile(File file) {
        if (file == null) {
            table.getColumns().clear();
            table.getItems().clear();
            return;
        }
        
        currentFile = file;
        table.getColumns().clear();
        table.getItems().clear();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                return;
            }

            // Trova l'ultima colonna con dati
            int lastColumn = 0;
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && !getCellValueAsString(cell).trim().isEmpty()) {
                    lastColumn = i;
                }
            }

            // Crea le colonne basate sull'intestazione
            for (int i = 0; i <= lastColumn; i++) {
                final int columnIndex = i;
                String headerText = getCellValueAsString(headerRow.getCell(i));
                if (headerText.trim().isEmpty()) {
                    headerText = "Colonna " + (i + 1);
                }
                
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(headerText);
                column.setCellValueFactory(data -> {
                    if (data.getValue() != null && columnIndex < data.getValue().size()) {
                        return new SimpleStringProperty(data.getValue().get(columnIndex));
                    }
                    return new SimpleStringProperty("");
                });
                
                // Stile delle colonne
                column.setStyle("-fx-alignment: center-left; " +
                              "-fx-font-size: 14px; " +
                              "-fx-padding: 8px;");
                
                // Imposta la larghezza minima e preferita
                column.setMinWidth(100);
                column.setPrefWidth(150);
                
                // Abilita il ridimensionamento
                column.setResizable(true);
                
                table.getColumns().add(column);
            }

            // Aggiungi i dati
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    ObservableList<String> rowData = FXCollections.observableArrayList();
                    for (int j = 0; j <= lastColumn; j++) {
                        rowData.add(getCellValueAsString(row.getCell(j)));
                    }
                    data.add(rowData);
                }
            }
            table.setItems(data);

            // Imposta l'altezza delle righe
            table.setFixedCellSize(35);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Formatta i numeri senza decimali se sono interi
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value)) {
                    return String.format("%.0f", value);
                }
                return String.valueOf(value);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    public File getCurrentFile() {
        return currentFile;
    }
} 