package com.automator.view.pages;

//package com.automator.view.components.ExcelTableView;

import java.io.File;

import com.automator.model.services.ExcelStorage;
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
    	// Header migliorato
        Label headerLabel = new Label("Gestione file EXCEL");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Caricamento ed eliminazione");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        VBox header = new VBox(5, headerLabel, subtitleLabel);
        header.setPadding(new Insets(25, 20, 25, 20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #2980b9); " +
                       "-fx-background-radius: 8;");
        // Cambia il colore del testo per il gradiente blu
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ecf0f1;");
        
        this.setTop(header);

        // Tabella centrale
        excelTableView = new ExcelTableView();
        this.setCenter(excelTableView);

        // Footer
        VBox footer = new VBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20));
        footer.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0;");

        fileNameLabel = new Label("Nessun file selezionato");
        fileNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Pulsanti
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button loadButton = new Button("Carica File Excel");
        stylePrimaryButton(loadButton);
        loadButton.setOnAction(e -> loadExcelFile());

        Button clearButton = new Button("Elimina File");
        styleDangerButton(clearButton);
        clearButton.setOnAction(e -> clearTable());

        buttonBox.getChildren().addAll(loadButton, clearButton);
        footer.getChildren().addAll(fileNameLabel, buttonBox);

        this.setBottom(footer);
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle(
            "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 10 20; -fx-background-radius: 8;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 10 20; -fx-background-radius: 8;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 10 20; -fx-background-radius: 8;"
        ));
    }

    private void styleDangerButton(Button button) {
        button.setStyle(
            "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 10 20; -fx-background-radius: 8;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 10 20; -fx-background-radius: 8;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 10 20; -fx-background-radius: 8;"
        ));
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
            fileNameLabel.setText("File selezionato: " + file.getName());
            ExcelStorage.getInstance().setFile(file);
            excelTableView.loadExcelFile(file);
        }
    }

    private void clearTable() {
        excelTableView.loadExcelFile(null);
        currentFile = null;
        fileNameLabel.setText("Nessun file selezionato");
    }
}
