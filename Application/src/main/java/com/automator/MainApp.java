package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        Tab siaeTab = new Tab("SIAE", new SiaePage());
        siaeTab.setClosable(false);

        Tab leaTab = new Tab("LEA", new LeaPage());
        leaTab.setClosable(false);

        tabPane.getTabs().addAll(siaeTab, leaTab);

        Scene scene = new Scene(tabPane, 800, 800);
        stage.setScene(scene);
        stage.setTitle("Fever Automator");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
