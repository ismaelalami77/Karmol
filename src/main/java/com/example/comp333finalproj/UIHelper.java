package com.example.comp333finalproj;



import javafx.scene.control.*;
import javafx.scene.text.Text;

public class UIHelper {

    private static final String fontFamily = "Times New Roman";

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
        Button myButton = new Button(content);
        String baseColor = "#001F54";
        String hoverColor = "#003366";

        String style = "-fx-background-color: " + baseColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-pref-width: 200;" +
                "-fx-pref-height: 60;" +
                "-fx-font-size: 25px; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-radius: 10px;";

        String hoverStyle = "-fx-background-color: " + hoverColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-pref-width: 200;" +
                "-fx-pref-height: 60;" +
                "-fx-font-size: 25px; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-radius: 10px;";

        myButton.setStyle(style);
        myButton.setOnMouseEntered(e -> myButton.setStyle(hoverStyle));
        myButton.setOnMouseExited(e -> myButton.setStyle(style));

        return myButton;
    }

    public static Button deleteStyledButton(String content) {
        Button myButton = new Button(content);
        String baseColor = "#001F54";
        String hoverColor = "#003366";

        String style = "-fx-background-color: " + baseColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-pref-width: 200;" +
                "-fx-pref-height: 60;" +
                "-fx-font-size: 20px; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-radius: 10px;";

        String hoverStyle = "-fx-background-color: " + hoverColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-pref-width: 200;" +
                "-fx-pref-height: 60;" +
                "-fx-font-size: 20px; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10px 20px; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-radius: 10px;";

        myButton.setStyle(style);
        myButton.setOnMouseEntered(e -> myButton.setStyle(hoverStyle));
        myButton.setOnMouseExited(e -> myButton.setStyle(style));

        return myButton;
    }

    public static TextField createStyledTextField() {
        TextField myTextField = new TextField();
        myTextField.setPrefSize(200, 60);
        myTextField.setStyle("-fx-background-radius: 15px; " +
                "-fx-background-color: white; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-text-fill: black; " +
                "-fx-font-size: 20px; " +
                "-fx-border-color: #001F54; " +
                "-fx-border-radius: 15px;");
        return myTextField;
    }

    public static Text createInfoText(String content) {
        Text infoText = new Text(content);
        infoText.setStyle("-fx-font-family: '" + fontFamily + "'; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 28px;");
        return infoText;
    }

    public static ComboBox<String> createComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();

        comboBox.setPrefSize(200, 60);
        comboBox.setStyle("-fx-background-radius: 15px; " +
                "-fx-background-color: white; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-border-color: #001F54; " +
                "-fx-border-radius: 15px; " +
                "-fx-font-size: 20px; " +
                "-fx-text-fill: black;");

        return comboBox;
    }

    public static Text radiButtonText(String content) {
        Text infoText = new Text(content);
        infoText.setStyle("-fx-font-family: '" + fontFamily + "'; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 15px;");
        return infoText;
    }

    public static RadioButton createRadioButton(String content) {
        RadioButton radioButton = new RadioButton(content);
        radioButton.setStyle("-fx-font-family: '" + fontFamily + "'; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: " + 15 + "px; " +
                "-fx-text-fill: black;");
        return radioButton;
    }

    public static DatePicker createStyledDatePicker() {
        DatePicker datePicker = new DatePicker();
        datePicker.setPrefSize(200, 60);
        datePicker.setStyle("-fx-background-radius: 15px; " +
                "-fx-background-color: white; " +
                "-fx-font-family: '" + fontFamily + "';" +
                "-fx-text-fill: black; " +
                "-fx-font-size: 20px; " +
                "-fx-border-color: #001F54; " +
                "-fx-border-radius: 15px;");
        return datePicker;
    }











}
