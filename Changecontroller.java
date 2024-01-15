package com.example.atm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

public class Changecontroller implements Initializable {

    @FXML
    TextField NewCardConfirm;
    @FXML
    TextField AccountNo;
    @FXML
    TextField OldPin;
    @FXML
    TextField NewPin;
    Stage stage;
    Scene scene;

    private String url;
    private String username;
    private String password;

    Alert alert;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDatabaseProperties();

        setTextFieldLengthLimit(AccountNo, 10);
        setTextFieldLengthLimit(OldPin, 4);
        setTextFieldLengthLimit(NewCardConfirm, 4);
        setTextFieldLengthLimit(NewPin, 4);

        String accountNo = AccountNo.getText();
        String oldPin = OldPin.getText();
        String newPin = NewPin.getText();
        String confirmPIn = NewCardConfirm.getText();
    }

    private void setTextFieldLengthLimit(TextField textField, int maxLength) {
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > maxLength) {
                return null;
            }
            return change;
        });

        textField.setTextFormatter(textFormatter);
    }

    public void ConfirmPin(ActionEvent event) throws IOException {
        String accountNo = AccountNo.getText();
        String oldPin = OldPin.getText();
        String newPin = NewPin.getText();
        String confirmPIn = NewCardConfirm.getText();

        if (!newPin.equals(confirmPIn)) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("New Passwords must be equal!!");
            alert.showAndWait();
        } else {
            if (CheckData() && changePin(accountNo, newPin, oldPin)) {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("PIN changed successfully!");
                alert.showAndWait();
                exit(event);
            } else {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("PIN change failed. Please check your input.");
                alert.showAndWait();
            }
        }
    }

    private boolean CheckData() {
        return true;
    }

    private boolean changePin(String accountNo, String pin, String old) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            if (verifyAccountAndPin(connection, accountNo, old)) {
                String updateQuery = "UPDATE card_information SET atmPIN = ? WHERE CustomerID = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setString(1, pin);
                    updateStatement.setString(2, accountNo);
                    updateStatement.executeUpdate();
                    return true;
                }
            } else {
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Invalid account number or old PIN.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean verifyAccountAndPin(Connection connection, String accountNumber, String pin) throws SQLException {
        String selectQuery = "SELECT * FROM card_information WHERE CustomerID = ? AND atmPIN = ?";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setString(1, accountNumber);
            selectStatement.setString(2, pin);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public void exit(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/First_Scene.fxml"));
        Parent roots = loader.load();

        Atm_controllr controller = loader.getController();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();
    }

    private void loadDatabaseProperties() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
            try {
                Properties prop = new Properties();

                if (input == null) {
                    System.out.println("Sorry, unable to find db.properties");
                    return;
                }

                prop.load(input);

                url = prop.getProperty("db.url");
                username = prop.getProperty("db.username");
                password = prop.getProperty("db.password");
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
