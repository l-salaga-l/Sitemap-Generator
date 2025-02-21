package org.example.sitemapgenerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("SiteMap Generator");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}