@echo off
set JAVAFX=C:\Users\admin\Downloads\openjfx-25.0.1_windows-x64_bin-sdk\javafx-sdk-25.0.1\lib
mvn clean package

java --module-path "%JAVAFX%" --add-modules javafx.controls,javafx.fxml -cp target/classes com.example.xmlvalidator.MainApp
