package com.automator.Pages;

import com.automator.controller.SiaeController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class SiaePage extends BorderPane {
    private final SiaeController siaeController;

    public SiaePage() {
        siaeController = new SiaeController();

        // Header
        Label headerLabel = new Label("Operazioni SIAE");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        VBox header = new VBox(headerLabel);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5;");
        this.setTop(header);

        // Area centrale con i pulsanti delle operazioni
        VBox operationsBox = new VBox(20);
        operationsBox.setPadding(new Insets(20));
        operationsBox.setAlignment(Pos.CENTER);
        operationsBox.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        // Pulsanti delle operazioni (con resultIcon)
        HBox op1 = createOperationBox("Operazione 1", "#3498db");
        HBox op2 = createOperationBox("Operazione 2", "#2ecc71");
        HBox op3 = createOperationBox("Operazione 3", "#e67e22");
        HBox op4 = createOperationBox("Operazione 4", "#9b59b6");

        operationsBox.getChildren().addAll(op1, op2, op3, op4);

        HBox centerWrapper = new HBox(operationsBox);
        centerWrapper.setAlignment(Pos.CENTER);
        this.setCenter(centerWrapper);
    }

    private HBox createOperationBox(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 30; -fx-background-radius: 5;",
                color
        ));
        button.setMaxWidth(300);

        Region resultIcon = createResultIcon();
        button.setOnAction(e -> {
            boolean success = handleOperation(text);
            updateIcon(resultIcon, success);
        });

        HBox box = new HBox(10, button, resultIcon);
        return box;
    }

    private boolean handleOperation(String operationName) {
        return siaeController.handleOperation(operationName);
        // TODO: Mostrare feedback all'utente
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
