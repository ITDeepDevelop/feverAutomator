package com.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

import java.io.File;

public class LeaPage extends VBox {

    private final Label fileNameLabel = new Label("Nessun file selezionato");

    public LeaPage() {
        Label pageTitle = new Label("LEA AUTOMATION PAGE");
        pageTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        HBox pageTitleBox = new HBox(pageTitle);
        pageTitleBox.setStyle("-fx-alignment: center;");
        this.getChildren().add(pageTitleBox);

        pageTitleBox.setPadding(new Insets(0, 0, 20, 0));


        // Upload file
        Label titleLabel = new Label("Upload excel");

        Button uploadButton = new Button("Seleziona file");
        uploadButton.setOnAction(e -> handleFileUpload());

        Button deleteButton = new Button("Elimina file");
        deleteButton.setOnAction(e -> handleFileDelete());

        // Layout principale
        this.setSpacing(10);
        this.setStyle("-fx-alignment: center; -fx-padding: 40 0 0 0;");

        titleLabel.setStyle("-fx-font-size: 18px;");
        fileNameLabel.setStyle("-fx-font-size: 16px;");
        uploadButton.setStyle("-fx-font-size: 14px;");
        deleteButton.setStyle("-fx-font-size: 14px;");

        this.getChildren().addAll(titleLabel, fileNameLabel, uploadButton, deleteButton);

        // Operazione 1
        Label opLabel1 = new Label("Operazione 1:");
        opLabel1.setStyle("-fx-font-size: 14px;");
        Button actionButton1 = new Button("Azione 1");
        actionButton1.setStyle("-fx-font-size: 14px;");
        Region resultIcon1 = createResultIcon();
        actionButton1.setOnAction(e -> updateIcon(resultIcon1, simulateOperation()));
        HBox box1 = new HBox(10, opLabel1, actionButton1, resultIcon1);
        box1.setStyle("-fx-alignment: center;");

        // Operazione 2
        Label opLabel2 = new Label("Operazione 2:");
        opLabel2.setStyle("-fx-font-size: 14px;");
        Button actionButton2 = new Button("Azione 2");
        actionButton2.setStyle("-fx-font-size: 14px;");
        Region resultIcon2 = createResultIcon();
        actionButton2.setOnAction(e -> updateIcon(resultIcon2, simulateOperation()));
        HBox box2 = new HBox(10, opLabel2, actionButton2, resultIcon2);
        box2.setStyle("-fx-alignment: center;");

        // Operazione 3
        Label opLabel3 = new Label("Operazione 3:");
        opLabel3.setStyle("-fx-font-size: 14px;");
        Button actionButton3 = new Button("Azione 3");
        actionButton3.setStyle("-fx-font-size: 14px;");
        Region resultIcon3 = createResultIcon();
        actionButton3.setOnAction(e -> updateIcon(resultIcon3, simulateOperation()));
        HBox box3 = new HBox(10, opLabel3, actionButton3, resultIcon3);
        box3.setStyle("-fx-alignment: center;");

        // Operazione 4
        Label opLabel4 = new Label("Operazione 4:");
        opLabel4.setStyle("-fx-font-size: 14px;");
        Button actionButton4 = new Button("Azione 4");
        actionButton4.setStyle("-fx-font-size: 14px;");
        Region resultIcon4 = createResultIcon();
        actionButton4.setOnAction(e -> updateIcon(resultIcon4, simulateOperation()));
        HBox box4 = new HBox(10, opLabel4, actionButton4, resultIcon4);
        box4.setStyle("-fx-alignment: center;");

        // VBox per le automazioni
        VBox automationBox = new VBox(10);
        automationBox.setStyle("-fx-alignment: center-left;");
        automationBox.setPadding(new Insets(30, 0, 0, 0));

        Label automationTitle = new Label("Execute automations");
        automationTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox titleBox = new HBox(automationTitle);
        titleBox.setStyle("-fx-alignment: center;");

        automationBox.getChildren().addAll(titleBox, box1, box2, box3, box4);
        this.getChildren().add(automationBox);
    }

    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona file Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
        );

        File file = fileChooser.showOpenDialog(this.getScene().getWindow());

        if (file != null) {
            fileNameLabel.setText(file.getName());
        }
    }

    private void handleFileDelete() {
        fileNameLabel.setText("Nessun file selezionato");
    }

    private boolean simulateOperation() {
        return Math.random() < 0.5;
    }

    private Region createResultIcon() {
        Region icon = new Region();
        icon.setMinSize(16, 16);
        icon.setMaxSize(16, 16);
        icon.setStyle("-fx-background-color: transparent; -fx-background-radius: 8px;");
        return icon;
    }

    private void updateIcon(Region icon, boolean success) {
        String color = success ? "green" : "red";
        icon.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8px;");
    }
}
