package com.automator.view.pages;

import java.util.concurrent.atomic.AtomicBoolean;

//package com.automator.Pages;

import com.automator.controller.LeaController;
import com.automator.model.services.CredenzialiManager;
import com.automator.model.services.Credenziale;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class LeaPage extends BorderPane {
	  private final LeaController leaController;
	    private final TextField emailField;
	    private final PasswordField passwordField;
	    private final TextField monthField;
	    private final TextField yearField;

	    private final CredenzialiManager credenzialiManager;
	    private final ListView<Credenziale> savedList;

	    public LeaPage() {
	        leaController = new LeaController();
	        credenzialiManager = new CredenzialiManager();

	        // ----------------- LOGIN -----------------
	        Label credentialsTitle = new Label("Credenziali di Accesso");
	        credentialsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

	        Label emailLabel = new Label("Email:");
	        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-weight: 500;");
	        emailField = new TextField();
	        emailField.setStyle("-fx-pref-height: 35px; -fx-font-size: 13px; " +
	                "-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
	        emailField.setPrefWidth(280);

	        Label passwordLabel = new Label("Password:");
	        passwordLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-font-weight: 500;");
	        passwordField = new PasswordField();
	        passwordField.setStyle("-fx-pref-height: 35px; -fx-font-size: 13px; " +
	                "-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
	        passwordField.setPrefWidth(240);

	        TextField passwordVisibleField = new TextField();
	        passwordVisibleField.setStyle(passwordField.getStyle());
	        passwordVisibleField.setPrefWidth(240);
	        passwordVisibleField.setVisible(false);
	        passwordVisibleField.setManaged(false);

	        Button togglePasswordButton = new Button("👁");
	        togglePasswordButton.setStyle("-fx-pref-width: 35px; -fx-pref-height: 35px; " +
	                "-fx-font-size: 14px; -fx-background-color: #ecf0f1; " +
	                "-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-cursor: hand;");

	        passwordField.textProperty().addListener((obs, o, n) -> {
	            if (!passwordVisibleField.isFocused()) passwordVisibleField.setText(n);
	        });
	        passwordVisibleField.textProperty().addListener((obs, o, n) -> {
	            if (!passwordField.isFocused()) passwordField.setText(n);
	        });

	        AtomicBoolean isPasswordVisible = new AtomicBoolean(false);
	        togglePasswordButton.setOnAction(e -> {
	            if (isPasswordVisible.get()) {
	                passwordVisibleField.setVisible(false);
	                passwordVisibleField.setManaged(false);
	                passwordField.setVisible(true);
	                passwordField.setManaged(true);
	                togglePasswordButton.setText("👁");
	                isPasswordVisible.set(false);
	            } else {
	                passwordField.setVisible(false);
	                passwordField.setManaged(false);
	                passwordVisibleField.setVisible(true);
	                passwordVisibleField.setManaged(true);
	                togglePasswordButton.setText("🙈");
	                isPasswordVisible.set(true);
	            }
	        });

	        StackPane passwordContainer = new StackPane(passwordField, passwordVisibleField);
	        HBox passwordBox = new HBox(5, passwordContainer, togglePasswordButton);
	        passwordBox.setAlignment(Pos.CENTER_LEFT);

	        // ----------------- LISTA CREDENZIALI -----------------
	        Label savedTitle = new Label("Credenziali salvate");
	        savedTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
	        savedList = new ListView<>();
	        savedList.setPrefHeight(150);
	        aggiornaListaCredenziali();

	        savedList.setCellFactory(lv -> new ListCell<>() {
	            @Override
	            protected void updateItem(Credenziale cred, boolean empty) {
	                super.updateItem(cred, empty);
	                if (empty || cred == null) {
	                    setText(null);
	                } else {
	                    setText(cred.getEmail() + " | ****");
	                }
	            }
	        });

	        savedList.setOnMouseClicked(event -> {
	            Credenziale selected = savedList.getSelectionModel().getSelectedItem();
	            if (selected != null) {
	                emailField.setText(selected.getEmail());
	                passwordField.setText(selected.getPassword());
	            }
	        });

	        VBox credentialsBox = new VBox(8,
	                credentialsTitle,
	                new VBox(5, emailLabel, emailField),
	                new VBox(5, passwordLabel, passwordBox),
	                savedTitle,
	                savedList
	        );
	        credentialsBox.setPadding(new Insets(20));
	        credentialsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
	                "-fx-border-color: #e9ecef; -fx-border-radius: 8;");
	        credentialsBox.setPrefWidth(320);

	        // ----------------- OPERAZIONI -----------------
	        Label operationsTitle = new Label("Operazioni Disponibili");
	        operationsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

	        HBox op1 = createOperationBox("Conferma Evento", "#3498db", "Conferma degli eventi");
	        HBox op2 = createOperationBox("Download Licenza", "#27ae60", "Scarica il PDF di una licenza");

	        VBox operationsBox = new VBox(15);
	        operationsBox.getChildren().addAll(operationsTitle, op1, op2);
	        operationsBox.setPadding(new Insets(20));
	        operationsBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
	                "-fx-border-color: #e9ecef; -fx-border-radius: 8; " +
	                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
	        operationsBox.setPrefWidth(420);

	        // ----------------- PERIODO -----------------
	        Label timeTitle = new Label("Periodo");
	        timeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

	        Label monthLabel = new Label("Mese:");
	        monthField = new TextField();
	        monthField.setPrefWidth(80);

	        Label yearLabel = new Label("Anno:");
	        yearField = new TextField();
	        yearField.setPrefWidth(80);

	        HBox dateInputsBox = new HBox(15,
	                new VBox(2, monthLabel, monthField),
	                new VBox(2, yearLabel, yearField)
	        );
	        dateInputsBox.setAlignment(Pos.CENTER);

	        VBox timeBox = new VBox(8, timeTitle, dateInputsBox);
	        timeBox.setPadding(new Insets(20));
	        timeBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e9ecef; -fx-border-radius: 8;");
	        timeBox.setMaxWidth(220);
	        timeBox.setAlignment(Pos.CENTER);

	        // ----------------- LAYOUT -----------------
	        HBox centerWrapper = new HBox(30, credentialsBox, operationsBox);
	        centerWrapper.setAlignment(Pos.CENTER);
	        centerWrapper.setPadding(new Insets(30, 20, 30, 20));

	        VBox mainWrapper = new VBox(20, centerWrapper, timeBox);
	        mainWrapper.setAlignment(Pos.CENTER);
	        mainWrapper.setStyle("-fx-background-color: #f5f6fa;");
	        mainWrapper.setPadding(new Insets(20));
	        this.setCenter(mainWrapper);
	    }

	    // ----------------- METODI OPERAZIONI -----------------
	    private HBox createOperationBox(String operationName, String color, String description) {
	        // Icona status (inizialmente neutrale)
	        Circle statusIcon = new Circle(8);
	        statusIcon.setFill(Color.web("#95a5a6"));
	        statusIcon.setStroke(Color.web("#7f8c8d"));
	        statusIcon.setStrokeWidth(1);

	        // Testo principale
	        Label nameLabel = new Label(operationName);
	        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
	        
	        // Descrizione
	        Label descLabel = new Label(description);
	        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
	        descLabel.setWrapText(true);
	        
	        VBox textBox = new VBox(2, nameLabel, descLabel);
	        textBox.setAlignment(Pos.CENTER_LEFT);

	        // Pulsante
	        Button actionButton = new Button("Esegui");
	        actionButton.setStyle(
	            "-fx-background-color: " + color + "; " +
	            "-fx-text-fill: white; " +
	            "-fx-font-size: 12px; " +
	            "-fx-font-weight: bold; " +
	            "-fx-background-radius: 20; " +
	            "-fx-min-width: 80px; " +
	            "-fx-pref-height: 30px; " +
	            "-fx-cursor: hand;"
	        );
	        
	        // Effetto hover
	        actionButton.setOnMouseEntered(e -> {
	            actionButton.setStyle(actionButton.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);");
	        });
	        actionButton.setOnMouseExited(e -> {
	            actionButton.setStyle(actionButton.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);", ""));
	        });

	        // Aggiungi gli event handler per i pulsanti
	        switch (operationName) {
	            case "Conferma Evento":
	                actionButton.setOnAction(e -> executeOperation1(statusIcon));
	                break;
	            case "Download Licenza":
	                actionButton.setOnAction(e -> executeOperation2(statusIcon));
	                break;
	        }

	        // Layout del box - MODIFICATO PER ALLINEARE I PULSANTI A SINISTRA
	        HBox operationBox = new HBox(15);
	        operationBox.getChildren().addAll(statusIcon, textBox, actionButton);
	        operationBox.setAlignment(Pos.CENTER_LEFT); // Allineamento generale a sinistra
	        operationBox.setPadding(new Insets(15, 20, 15, 20));
	        operationBox.setStyle(
	            "-fx-background-color: white; " +
	            "-fx-background-radius: 8; " +
	            "-fx-border-color: #e9ecef; " +
	            "-fx-border-radius: 8; " +
	            "-fx-border-width: 1;"
	        );
	        
	        // Imposta una larghezza fissa per il textBox così i pulsanti si allineano
	        textBox.setPrefWidth(280); // Larghezza fissa per allineare i pulsanti
	        textBox.setMaxWidth(280);
	        
	        // Effetto hover per tutto il box
	        operationBox.setOnMouseEntered(e -> {
	            operationBox.setStyle(operationBox.getStyle() + 
	                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); " +
	                "-fx-border-color: " + color + ";");
	        });
	        operationBox.setOnMouseExited(e -> {
	            operationBox.setStyle(
	                "-fx-background-color: white; " +
	                "-fx-background-radius: 8; " +
	                "-fx-border-color: #e9ecef; " +
	                "-fx-border-radius: 8; " +
	                "-fx-border-width: 1;"
	            );
	        });

	        return operationBox;
	    }

	    private void executeOperation1(Circle statusIcon) {
	        updateStatusIcon(statusIcon, "running");
	        String email = emailField.getText();
	        String password = passwordField.getText();
	        boolean success = leaController.handleOperation("Conferma Evento", email, password, null, null);
	        if (success) credenzialiManager.aggiungi(email, password);
	        aggiornaListaCredenziali();
	        updateStatusIcon(statusIcon, success ? "success" : "error");
	    }

	    private void executeOperation2(Circle statusIcon) {
	        updateStatusIcon(statusIcon, "running");
	        String email = emailField.getText();
	        String password = passwordField.getText();
	        String month = monthField.getText();
	        String year = yearField.getText();
	        boolean success = leaController.handleOperation("Download Licenza", email, password, month, year);
	        if (success) credenzialiManager.aggiungi(email, password);
	        aggiornaListaCredenziali();
	        updateStatusIcon(statusIcon, success ? "success" : "error");
	    }

	    private void updateStatusIcon(Circle icon, String status) {
	        Platform.runLater(() -> {
	            switch (status) {
	                case "running" -> { icon.setFill(Color.web("#f39c12")); icon.setStroke(Color.web("#e67e22")); }
	                case "success" -> { icon.setFill(Color.web("#27ae60")); icon.setStroke(Color.web("#229954")); }
	                case "error"   -> { icon.setFill(Color.web("#e74c3c")); icon.setStroke(Color.web("#c0392b")); }
	                default        -> { icon.setFill(Color.web("#95a5a6")); icon.setStroke(Color.web("#7f8c8d")); }
	            }
	        });
	    }

	    private void aggiornaListaCredenziali() {
	        savedList.getItems().setAll(credenzialiManager.getAll());
	    }
}
