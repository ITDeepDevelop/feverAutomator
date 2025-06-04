package com.automator.view.pages;

import java.util.concurrent.atomic.AtomicBoolean;

//package com.automator.Pages;

import com.automator.controller.LeaController;
import com.automator.controller.SiaeController;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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



    public LeaPage() {
        leaController = new LeaController();

        // Header migliorato
        Label headerLabel = new Label("Operazioni LEA");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Gestione automatizzata *POI VEDIAMO DI COSA*");
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

        // Sezione credenziali migliorata
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

        // Campo password nascosto
        passwordField = new PasswordField();
        passwordField.setStyle("-fx-pref-height: 35px; -fx-font-size: 13px; " +
            "-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        passwordField.setPrefWidth(240);

        // Campo password visibile (inizialmente nascosto)
        TextField passwordVisibleField = new TextField();
        passwordVisibleField.setStyle("-fx-pref-height: 35px; -fx-font-size: 13px; " +
            "-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        passwordVisibleField.setPrefWidth(240);
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);

        // Pulsante per toggle visibilità
        Button togglePasswordButton = new Button("👁");
        togglePasswordButton.setStyle("-fx-pref-width: 35px; -fx-pref-height: 35px; " +
            "-fx-font-size: 14px; -fx-background-color: #ecf0f1; " +
            "-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5; " +
            "-fx-cursor: hand;");

        // Sincronizzazione tra i due campi
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!passwordVisibleField.isFocused()) {
                passwordVisibleField.setText(newText);
            }
        });

        passwordVisibleField.textProperty().addListener((obs, oldText, newText) -> {
            if (!passwordField.isFocused()) {
                passwordField.setText(newText);
            }
        });

        // Azione del pulsante toggle
        AtomicBoolean isPasswordVisible = new AtomicBoolean(false);
        togglePasswordButton.setOnAction(e -> {
            if (isPasswordVisible.get()) {
                // Nasconde la password
                passwordVisibleField.setVisible(false);
                passwordVisibleField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                togglePasswordButton.setText("👁");
                isPasswordVisible.set(false);
            } else {
                // Mostra la password
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                passwordVisibleField.setVisible(true);
                passwordVisibleField.setManaged(true);
                togglePasswordButton.setText("🙈");
                isPasswordVisible.set(true);
            }
        });

        // Container per password e pulsante
        StackPane passwordContainer = new StackPane();
        passwordContainer.getChildren().addAll(passwordField, passwordVisibleField);

        HBox passwordBox = new HBox(5);
        passwordBox.getChildren().addAll(passwordContainer, togglePasswordButton);
        passwordBox.setAlignment(Pos.CENTER_LEFT);

        VBox credentialsBox = new VBox(8);
        credentialsBox.getChildren().addAll(
            credentialsTitle,
            new VBox(5, emailLabel, emailField),
            new VBox(5, passwordLabel, passwordBox)
        );
        credentialsBox.setPadding(new Insets(20));
        credentialsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
            "-fx-border-color: #e9ecef; -fx-border-radius: 8;");
        credentialsBox.setPrefWidth(320);

        // Sezione operazioni migliorata
        Label operationsTitle = new Label("Operazioni Disponibili");
        operationsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        HBox op1 = createOperationBox("Operazione 1", "#3498db", "Operazione di test");
        HBox op2 = createOperationBox("Download Licenza", "#27ae60", "Scarica il PDF di una licenza");

        VBox operationsBox = new VBox(15);
        operationsBox.getChildren().addAll(operationsTitle, op1, op2);
        operationsBox.setPadding(new Insets(20));
        operationsBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                              "-fx-border-color: #e9ecef; -fx-border-radius: 8; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        operationsBox.setPrefWidth(420);

        // Layout centrale migliorato
        HBox centerWrapper = new HBox(30, credentialsBox, operationsBox);
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.setPadding(new Insets(30, 20, 30, 20));

        // Wrapper con sfondo
        VBox mainWrapper = new VBox(centerWrapper);
        mainWrapper.setStyle("-fx-background-color: #f5f6fa;");
        
        this.setCenter(mainWrapper);
    }

 // Metodo helper migliorato per creare i box delle operazioni
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
            case "Operazione 1":
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

    // Metodi di esempio per le operazioni (aggiorna questi con la tua logica)
    private void executeOperation1(Circle statusIcon) {
        updateStatusIcon(statusIcon, "running");
        // Esegui operazione 1
        boolean success = leaController.handleOperation("Op1", null, null);
        updateStatusIcon(statusIcon, success ? "success" : "error");
    }

    private void executeOperation2(Circle statusIcon) {
        updateStatusIcon(statusIcon, "running");
        // Prendi le credenziali dai campi
        String email = emailField.getText();
        String password = passwordField.getText();
        
        // Esegui operazione 2 (Accettazione Permessi)
        boolean success = leaController.handleOperation("Download Licenza", email, password);
        updateStatusIcon(statusIcon, success ? "success" : "error");
    }

    // Metodo helper per aggiornare l'icona di status
    private void updateStatusIcon(Circle icon, String status) {
        Platform.runLater(() -> {
            switch (status) {
                case "running":
                    icon.setFill(Color.web("#f39c12"));
                    icon.setStroke(Color.web("#e67e22"));
                    break;
                case "success":
                    icon.setFill(Color.web("#27ae60"));
                    icon.setStroke(Color.web("#229954"));
                    break;
                case "error":
                    icon.setFill(Color.web("#e74c3c"));
                    icon.setStroke(Color.web("#c0392b"));
                    break;
                default:
                    icon.setFill(Color.web("#95a5a6"));
                    icon.setStroke(Color.web("#7f8c8d"));
            }
        });
    }
}
