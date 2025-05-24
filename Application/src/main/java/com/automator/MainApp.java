package com.automator;

import com.automator.view.pages.ExcelPage;
import com.automator.view.pages.LeaPage;
import com.automator.view.pages.SiaePage;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        
        // Header con logo e titolo
        HBox header = new HBox(15);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50; -fx-alignment: center-left;");
        
        // Logo
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
        logo.setFitHeight(40);
        logo.setPreserveRatio(true);
        
        // Titolo
        Label headerLabel = new Label("Fever Automator");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        header.getChildren().addAll(logo, headerLabel);
        root.setTop(header);

        // TabPane con stile
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white; -fx-background-insets: 0;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab siaeTab = new Tab("SIAE", new SiaePage());
        Tab leaTab = new Tab("LEA", new LeaPage());
        Tab excelTab = new Tab("Excel", new ExcelPage());

        tabPane.getTabs().addAll(siaeTab, leaTab, excelTab);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1000, 800);
        
        // Stili CSS aggiornati
        String css = ".tab-pane .tab-header-area .tab-header-background {\n" +
                    "    -fx-background-color: #34495e;\n" +
                    "}\n" +
                    ".tab-pane .tab {\n" +
                    "    -fx-background-color: #2c3e50;\n" +
                    "}\n" +
                    ".tab-pane .tab:selected {\n" +
                    "    -fx-background-color: #3498db;\n" +
                    "}\n" +
                    ".tab .tab-label {\n" +
                    "    -fx-text-fill: white;\n" +
                    "    -fx-font-weight: bold;\n" +
                    "}\n" +
                    ".tab:selected .tab-label {\n" +
                    "    -fx-text-fill: white;\n" +
                    "}\n" +
                    ".root {\n" +
                    "    -fx-background-color: #ecf0f1;\n" +
                    "}\n" +
                    ".button {\n" +
                    "    -fx-background-radius: 5;\n" +
                    "    -fx-font-weight: bold;\n" +
                    "}\n" +
                    ".button:hover {\n" +
                    "    -fx-opacity: 0.9;\n" +
                    "}\n" +
                    ".table-view {\n" +
                    "    -fx-background-color: white;\n" +
                    "    -fx-table-cell-border-color: #bdc3c7;\n" +
                    "}\n" +
                    ".table-view .column-header {\n" +
                    "    -fx-background-color: #34495e;\n" +
                    "}\n" +
                    ".table-view .column-header .label {\n" +
                    "    -fx-text-fill: white;\n" +
                    "    -fx-font-weight: bold;\n" +
                    "}";
        scene.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(css.getBytes()));

        stage.setScene(scene);
        stage.setTitle("Fever Automator");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
