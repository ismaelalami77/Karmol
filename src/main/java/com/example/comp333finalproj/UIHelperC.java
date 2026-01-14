package com.example.comp333finalproj;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class UIHelperC {

    private static final String fontFamily = "Times New Roman";
    private static final double fieldWidth = 250;
    private static final double fieldHeight = 50;
    private static final String accentColor = "#00a650";
    private static final String hoverColor = "#009144";


    public static void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }


    public static Text createTitleText(String content) {
        Text myText = new Text(content);
        myText.setStyle("-fx-font-family: '" + fontFamily + "'; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 40px;");
        return myText;
    }


    public static Button createStyledButton(String content) {
        Button button = new Button(content);


        button.setPrefSize(fieldWidth, fieldHeight);
        button.setMinSize(fieldWidth, fieldHeight);
        button.setMaxSize(fieldWidth, fieldHeight);

        String style = "-fx-background-color: " + accentColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 30px; " +
                "-fx-border-radius: 30px; " +
                "-fx-border-color: " + accentColor + "; " +
                "-fx-background-insets: 0;" +
                "-fx-padding: 0;";

        String hoverStyle = "-fx-background-color: " + hoverColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 20px; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 30px; " +
                "-fx-border-radius: 30px; " +
                "-fx-border-color: " + hoverColor + "; " +
                "-fx-background-insets: 0;" +
                "-fx-padding: 0;";

        button.setStyle(style);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(style));

        return button;
    }


    public static TextField createStyledTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);

        textField.setPrefSize(fieldWidth, fieldHeight);
        textField.setMinSize(fieldWidth, fieldHeight);
        textField.setMaxSize(fieldWidth, fieldHeight);

        textField.setStyle("-fx-background-radius: 30px; " +
                "-fx-background-color: white; " +
                "-fx-font-family: '" + fontFamily + "'; " +
                "-fx-font-size: 20px; " +
                "-fx-text-fill: black; " +
                "-fx-border-color: " + accentColor + "; " +
                "-fx-border-radius: 30px; " +
                "-fx-background-insets: 0;");

        return textField;
    }


    public static PasswordField createStyledPassField(String placeholder) {
        PasswordField passField = new PasswordField();
        passField.setPromptText(placeholder);

        passField.setPrefSize(fieldWidth, fieldHeight);
        passField.setMinSize(fieldWidth, fieldHeight);
        passField.setMaxSize(fieldWidth, fieldHeight);

        passField.setStyle("-fx-background-radius: 30px; " +
                "-fx-background-color: white; " +
                "-fx-font-family: '" + fontFamily + "'; " +
                "-fx-font-size: 20px; " +
                "-fx-text-fill: black; " +
                "-fx-border-color: " + accentColor + "; " +
                "-fx-border-radius: 30px; " +
                "-fx-background-insets: 0;");

        return passField;
    }

    public static Text createInfoText(String content) {
        Text infoText = new Text(content);
        infoText.setStyle("-fx-font-family: '" + fontFamily + "'; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 28px;");
        return infoText;
    }
}
