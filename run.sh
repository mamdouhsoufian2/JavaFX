#!/bin/bash
JAVAFX=/opt/javafx-sdk-21/lib
mvn clean package
java --module-path "$JAVAFX" --add-modules javafx.controls,javafx.fxml -cp target/classes com.example.xmlvalidator.MainApp
