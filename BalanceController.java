package com.example.atm;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class BalanceController implements Initializable {

    private String url;
    private String username;
    private String password;

    @FXML
    TextField AccountNo;
    @FXML
    TextField BalanceChecked;
    NumberFormat currencyFormat;
    Stage stage;
    Scene scene;

    public void balancebutton(ActionEvent event) {
        String account = AccountNo.getText();

        try (Connection connection = getConnection()) {
            if (isAccountAvailable(connection, account)) {
                double balance = getBalance(connection, account);
                BalanceChecked.setText(String.valueOf(balance));
            } else {
                showAlert("Account not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error accessing the database.");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private boolean isAccountAvailable(Connection connection, String accountNumber) throws SQLException {
        String selectQuery = "SELECT * FROM balance_table_customerid WHERE CustomerID = ?";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setString(1, accountNumber);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private double getBalance(Connection connection, String accountNumber) throws SQLException {
        String selectQuery = "SELECT balance FROM balance_table_customerid WHERE CustomerID = ?";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setString(1, accountNumber);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("Balance");
                } else {
                    return 0.0;
                }
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDatabaseProperties();
        BalanceChecked.setFont(new javafx.scene.text.Font(23));
        BalanceChecked.setStyle("-fx-text-fill: red;");
        BalanceChecked.setEditable(false);
        addAmountFormatter();
    }

    private void addAmountFormatter() {
        BalanceChecked.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                setCurrency(newValue);
            }
        });
    }

    public void setCurrency(String value) {
        try {
            String numericValue = value.replaceAll("[^\\d.]", "");
            double amount = Double.parseDouble(numericValue);

            currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "NG"));
            String formattedAmount = currencyFormat.format(amount);

            BalanceChecked.setText(formattedAmount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void Exitbutton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/First_Scene.fxml"));
        Parent roots = loader.load();

        Atm_controllr controller = loader.getController();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();
    }

    private void loadDatabaseProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find db.properties");
                return;
            }

            prop.load(input);

            url = prop.getProperty("db.url");
            username = prop.getProperty("db.username");
            password = prop.getProperty("db.password");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
