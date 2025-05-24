package com.automator.Pages;

import com.automator.controller.SiaeController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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

        // Pulsanti delle operazioni
        Button operation1Button = createOperationButton("Operazione 1", "#3498db");
        Button operation2Button = createOperationButton("Operazione 2", "#2ecc71");
        Button operation3Button = createOperationButton("Operazione 3", "#e67e22");
        Button operation4Button = createOperationButton("Operazione 4", "#9b59b6");

        operationsBox.getChildren().addAll(
            operation1Button,
            operation2Button,
            operation3Button,
            operation4Button
        );

        this.setCenter(operationsBox);
    }

    private Button createOperationButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 30; -fx-background-radius: 5;",
            color
        ));
        button.setMaxWidth(300);
        button.setOnAction(e -> handleOperation(text));
        return button;
    }

    private void handleOperation(String operationName) {
        boolean success = siaeController.handleOperation(operationName);
        // TODO: Mostrare feedback all'utente
    }
}
