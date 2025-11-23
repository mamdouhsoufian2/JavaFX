package com.example.xmlvalidator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Controller {

    @FXML
    private TextField xmlPathField;

    @FXML
    private TableView<File> xsdTable;

    @FXML
    private TableColumn<File, String> xsdNameColumn;

    @FXML
    private TableColumn<File, String> xsdPathColumn;

    @FXML
    private TextArea resultArea;

    // --- Données ---
    private File selectedXmlFile;
    private final ObservableList<File> xsdFiles = FXCollections.observableArrayList();
    private final ValidatorService validatorService = new ValidatorService();

    // --- Initialisation ---
    @FXML
    public void initialize() {
        // Lier la liste observable au tableau
        xsdTable.setItems(xsdFiles);

        // Configurer les colonnes du tableau pour afficher les infos des fichiers
        xsdNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        xsdPathColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAbsolutePath()));

        // Message de bienvenue
        resultArea.setText("Prêt. Veuillez sélectionner un fichier XML et ajouter des schémas XSD.");
    }


    // 1. Sélection du fichier XML [cite: 16]
    @FXML
    private void handleSelectXml() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier XML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers XML", "*.xml"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedXmlFile = file;
            xmlPathField.setText(file.getAbsolutePath());
            log("Fichier XML sélectionné : " + file.getName());
        }
    }

    // 2. Ajout d'un schéma XSD [cite: 18]
    @FXML
    private void handleAddXsd() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ajouter un schéma XSD");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers XSD", "*.xsd"));

        // Ouvrir dans le même dossier que le XML si possible (ergonomie)
        if (selectedXmlFile != null) {
            fileChooser.setInitialDirectory(selectedXmlFile.getParentFile());
        }

        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (files != null) {
            xsdFiles.addAll(files);
            log(files.size() + " schéma(s) ajouté(s).");
        }
    }

    // 3. Retirer un schéma de la liste
    @FXML
    private void handleRemoveXsd() {
        File selected = xsdTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            xsdFiles.remove(selected);
            log("Schéma retiré : " + selected.getName());
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un schéma dans la liste à retirer.");
        }
    }

    // 4. Validation [cite: 22]
    @FXML
    private void handleValidate() {
        if (selectedXmlFile == null) {
            showAlert("Erreur", "Veuillez d'abord sélectionner un fichier XML.");
            return;
        }
        if (xsdFiles.isEmpty()) {
            showAlert("Erreur", "Veuillez ajouter au moins un schéma XSD.");
            return;
        }

        log("--- Début de la validation ---");
        resultArea.clear(); // Nettoyer la zone de log

        // Appel au service de validation
        ValidatorService.ValidationResult result = validatorService.validate(selectedXmlFile, xsdFiles);

        // Affichage des résultats [cite: 27, 28, 29]
        if (result.isValid()) {
            log("SUCCÈS : Le fichier XML est valide par rapport aux schémas fournis.");
            showAlert("Validation Réussie", "Le document est valide !");
        } else {
            log("ÉCHEC : Le fichier XML contient des erreurs (" + result.getErrors().size() + ") :");
            for (String error : result.getErrors()) {
                log("- " + error);
            }
            showAlert("Echec de validation", "Le document contient des erreurs. Voir les logs pour plus de détails.");
        }
        log("--- Fin de la validation ---");
    }

    // 5. Export des résultats
    @FXML
    private void handleExport() {
        if (resultArea.getText().isEmpty()) {
            showAlert("Information", "Il n'y a rien à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport de validation");
        fileChooser.setInitialFileName("rapport_validation.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Texte", "*.txt"));

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(resultArea.getText());
                log("Rapport exporté avec succès vers : " + file.getName());
            } catch (IOException e) {
                showAlert("Erreur Export", "Impossible d'écrire le fichier : " + e.getMessage());
            }
        }
    }


    private void log(String message) {
        resultArea.appendText(message + "\n");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}