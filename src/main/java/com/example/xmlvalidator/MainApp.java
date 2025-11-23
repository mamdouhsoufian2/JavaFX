package com.example.xmlvalidator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Chargement du fichier interface view.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("view.fxml"));

        // Création de la scène (Largeur: 800px, Hauteur: 600px)
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        // Titre de la fenêtre
        stage.setTitle("Validateur XML Multi-Schémas");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}