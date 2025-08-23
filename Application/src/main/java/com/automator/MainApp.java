package com.automator;

import com.automator.view.pages.ExcelPage;
import com.automator.view.pages.LeaPage;
import com.automator.view.pages.SiaePage;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        
        // Header responsivo con logo e titolo
        HBox header = createResponsiveHeader();
        root.setTop(header);

        // TabPane con stile
        TabPane tabPane = createResponsiveTabPane();
        root.setCenter(tabPane);

        // Dimensioni iniziali responsive
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(1000, screenBounds.getWidth() * 0.9);
        double initialHeight = Math.min(700, screenBounds.getHeight() * 0.8);
        
        Scene scene = new Scene(root, initialWidth, initialHeight);
        
        // Dimensioni minime per evitare che diventi troppo piccola
        stage.setMinWidth(400);
        stage.setMinHeight(300);
        
        // CSS responsivo
        scene.getStylesheets().add("data:text/css;base64," + 
            java.util.Base64.getEncoder().encodeToString(getResponsiveCSS().getBytes()));

        // Listener per adattare l'interfaccia alla dimensione della finestra
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateResponsiveLayout(header, newVal.doubleValue());
        });

        stage.setScene(scene);
        stage.setTitle("Fever Automator");
        //stage.setMaximized(screenBounds.getWidth() <= 1024 || screenBounds.getHeight() <= 768);
        stage.setMaximized(false);
        stage.show();
    }
    
    private HBox createResponsiveHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setStyle("-fx-background-color: #2c3e50; -fx-alignment: center-left;");
        header.setSpacing(10);
        
        // Logo con dimensioni responsive
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
        logo.setFitHeight(32);
        logo.setPreserveRatio(true);
        logo.setSmooth(true);
        logo.setCache(true);
        
        // Titolo con dimensione responsive
        Label headerLabel = new Label("Fever Automator");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        headerLabel.getStyleClass().add("header-title");
        
        header.getChildren().addAll(logo, headerLabel);
        
        // Proprietà responsive per il logo
        header.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < 500) {
                logo.setFitHeight(24);
                headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
            } else if (newVal.doubleValue() < 600) {
                logo.setFitHeight(28);
                headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
            } else {
                logo.setFitHeight(32);
                headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
            }
        });
        
        return header;
    }
    
    private TabPane createResponsiveTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white; -fx-background-insets: 0;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // ScrollPane per SIAE con configurazione responsiva
        ScrollPane siaeScroll = createResponsiveScrollPane(new SiaePage());
        
        // ScrollPane per LEA con configurazione responsiva
        ScrollPane leaScroll = createResponsiveScrollPane(new LeaPage());

        // ScrollPane per Excel con configurazione specifica
        ScrollPane excelScroll = new ScrollPane(new ExcelPage());
        excelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        excelScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        excelScroll.setFitToHeight(true);
        excelScroll.setFitToWidth(true);

        Tab siaeTab = new Tab("SIAE", siaeScroll);
        Tab leaTab = new Tab("LEA", leaScroll);
        Tab excelTab = new Tab("Excel", excelScroll);
        
        // Tab responsive - su schermi piccoli usa icone o abbreviazioni
        Platform.runLater(() -> {
            Scene scene = tabPane.getScene();
            if (scene != null) {
                scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() < 500) {
                        siaeTab.setText("SI");
                        leaTab.setText("LE");
                        excelTab.setText("EX");
                    } else if (newVal.doubleValue() < 600) {
                        siaeTab.setText("SIAE");
                        leaTab.setText("LEA");
                        excelTab.setText("XLS");
                    } else {
                        siaeTab.setText("SIAE");
                        leaTab.setText("LEA");
                        excelTab.setText("Excel");
                    }
                });
            }
        });

        tabPane.getTabs().addAll(siaeTab, leaTab, excelTab);
        return tabPane;
    }
    
    private ScrollPane createResponsiveScrollPane(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        
        // Ottimizzazioni per performance su schermi piccoli
        scrollPane.setPannable(true);
        scrollPane.setCache(true);
        scrollPane.setCacheHint(CacheHint.SPEED);
        
        return scrollPane;
    }
    
    private void updateResponsiveLayout(HBox header, double width) {
        if (width < 500) {
            header.setPadding(new Insets(8, 10, 8, 10));
            header.setSpacing(8);
        } else if (width < 600) {
            header.setPadding(new Insets(10, 12, 10, 12));
            header.setSpacing(10);
        } else {
            header.setPadding(new Insets(10, 15, 10, 15));
            header.setSpacing(15);
        }
    }
    
    private String getResponsiveCSS() {
        return ".tab-pane .tab-header-area .tab-header-background {\n" +
                "    -fx-background-color: #34495e;\n" +
                "}\n" +
                ".tab-pane .tab {\n" +
                "    -fx-background-color: #2c3e50;\n" +
                "    -fx-min-width: 40px;\n" +
                "    -fx-pref-width: -1;\n" +
                "}\n" +
                ".tab-pane .tab:selected {\n" +
                "    -fx-background-color: #3498db;\n" +
                "}\n" +
                ".tab .tab-label {\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-font-weight: bold;\n" +
                "    -fx-font-size: 12px;\n" +
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
                "    -fx-min-height: 28px;\n" +
                "    -fx-pref-height: 32px;\n" +
                "    -fx-font-size: 12px;\n" +
                "}\n" +
                ".button:hover {\n" +
                "    -fx-opacity: 0.9;\n" +
                "}\n" +
                ".table-view {\n" +
                "    -fx-background-color: white;\n" +
                "    -fx-table-cell-border-color: #bdc3c7;\n" +
                "    -fx-font-size: 11px;\n" +
                "}\n" +
                ".table-view .column-header {\n" +
                "    -fx-background-color: #34495e;\n" +
                "    -fx-min-height: 25px;\n" +
                "}\n" +
                ".table-view .column-header .label {\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-font-weight: bold;\n" +
                "    -fx-font-size: 11px;\n" +
                "}\n" +
                ".table-row-cell {\n" +
                "    -fx-cell-size: 25px;\n" +
                "}\n" +
                ".header-title {\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-font-weight: bold;\n" +
                "}\n" +
                "/* Responsive breakpoints */\n" +
                "@media screen and (max-width: 600px) {\n" +
                "    .button {\n" +
                "        -fx-font-size: 10px;\n" +
                "        -fx-min-height: 24px;\n" +
                "        -fx-pref-height: 28px;\n" +
                "    }\n" +
                "    .table-view {\n" +
                "        -fx-font-size: 10px;\n" +
                "    }\n" +
                "    .table-view .column-header .label {\n" +
                "        -fx-font-size: 10px;\n" +
                "    }\n" +
                "    .table-row-cell {\n" +
                "        -fx-cell-size: 22px;\n" +
                "    }\n" +
                "}\n" +
                ".scroll-pane {\n" +
                "    -fx-fit-to-width: true;\n" +
                "    -fx-fit-to-height: true;\n" +
                "}\n" +
                ".scroll-pane .viewport {\n" +
                "    -fx-background-color: transparent;\n" +
                "}\n" +
                ".scroll-bar:horizontal .track,\n" +
                ".scroll-bar:vertical .track {\n" +
                "    -fx-background-color: #ecf0f1;\n" +
                "    -fx-border-color: #bdc3c7;\n" +
                "    -fx-background-radius: 0;\n" +
                "    -fx-border-radius: 0;\n" +
                "}\n" +
                ".scroll-bar:horizontal .thumb,\n" +
                ".scroll-bar:vertical .thumb {\n" +
                "    -fx-background-color: #7f8c8d;\n" +
                "    -fx-background-radius: 0;\n" +
                "}\n" +
                ".scroll-bar:horizontal .thumb:hover,\n" +
                ".scroll-bar:vertical .thumb:hover {\n" +
                "    -fx-background-color: #95a5a6;\n" +
                "}";
    }

    public static void main(String[] args) {
        launch();
    }
}