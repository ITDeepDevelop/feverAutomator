package com.automator.Pages;

import com.automator.controller.LeaController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class LeaPage extends BorderPane {

    private final LeaController leaController;

    public LeaPage() {
        leaController = new LeaController();
        // Header
        Label headerLabel = new Label("Operazioni LEA");
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
        HBox operation1Button = createOperationButton("Operazione 1", "#3498db", "Op1");
        HBox operation2Button = createOperationButton("Operazione 2", "#3498db", "Op2");
        HBox operation3Button = createOperationButton("Operazione 3", "#3498db", "Op3");
        HBox operation4Button = createOperationButton("Operazione 4", "#3498db", "Op4");

        operationsBox.getChildren().addAll(
            operation1Button,
            operation2Button,
            operation3Button,
            operation4Button
        );

        HBox centerWrapper = new HBox(operationsBox);
        centerWrapper.setAlignment(Pos.CENTER);
        this.setCenter(centerWrapper);

    }

    private HBox createOperationButton(String text, String color, String operationName) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 30; -fx-background-radius: 5;",
            color
        ));
        button.setMaxWidth(300);
        Region resultIcon = createResultIcon();
        button.setOnAction(e -> {
            boolean result = handleOperation(operationName);
            updateIcon(resultIcon, result);
        });

        HBox box = new HBox(10, button, resultIcon);
        return box;
    }

    private boolean handleOperation(String operationName) {
        return leaController.handleOperation(operationName);
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
