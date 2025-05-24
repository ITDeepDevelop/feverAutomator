package com.automator.view.pages;

//package com.automator.view.components.ExcelTableView;

import java.io.File;

import com.automator.view.components.ExcelTableView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ExcelPage extends BorderPane {
    private final ExcelTableView excelTableView;
    private File currentFile;
    private final Label fileNameLabel;

    public ExcelPage() {
        // Header
        Label headerLabel = new Label("Gestione File Excel");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        VBox header = new VBox(headerLabel);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");
        this.setTop(header);

        // Area centrale con la tabella Excel
        excelTableView = new ExcelTableView();
        this.setCenter(excelTableView);

        // Area inferiore con i pulsanti
        VBox bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(20));
        bottomBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");

        // Label per il nome del file
        fileNameLabel = new Label("Nessun file selezionato");
        fileNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        // Pulsanti
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button loadButton = new Button("Carica File Excel");
        loadButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        loadButton.setOnAction(e -> loadExcelFile());

        Button clearButton = new Button("Elimina File");
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        clearButton.setOnAction(e -> clearTable());

        buttonBox.getChildren().addAll(loadButton, clearButton);
        bottomBox.getChildren().addAll(fileNameLabel, buttonBox);
        this.setBottom(bottomBox);
    }

    private void loadExcelFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona File Excel");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("File Excel", "*.xlsx", "*.xls")
        );

        Stage stage = (Stage) this.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            currentFile = file;
            fileNameLabel.setText(file.getName());
            excelTableView.loadExcelFile(file);
        }
    }

    private void clearTable() {
        excelTableView.loadExcelFile(null);
        currentFile = null;
        fileNameLabel.setText("Nessun file selezionato");
    }
} 