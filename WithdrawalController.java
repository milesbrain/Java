package com.example.atm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Optional;
import java.util.Properties;

public class WithdrawalController {
    @FXML
    TextField AccountNo;
    Stage stage;
    Scene scene;

    private String url;
    private String username;
    private String password;

    public WithdrawalController() {
        loadDatabaseProperties();
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

    public void button500(ActionEvent event) throws IOException {
        handleWithdrawal(500);
    }

    public void button1000(ActionEvent event) throws IOException {
        handleWithdrawal(1000);
    }

    public void button2000(ActionEvent event) {
        handleWithdrawal(2000);
    }

    public void button10000(ActionEvent event) {
        handleWithdrawal(10000);
    }

    public void button5000(ActionEvent event) {
        handleWithdrawal(5000);
    }

    public void button3000(ActionEvent event) {
        handleWithdrawal(3000);
    }

    private void handleWithdrawal(double withdrawalAmount) {
        String accountNumber = AccountNo.getText();

        try (Connection connection = getConnection()) {
            if (isAccountAvailable(connection, accountNumber)) {
                double currentBalance = getBalance(connection, accountNumber);

                if (currentBalance >= withdrawalAmount) {
                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setContentText("Do you want to withdraw " + withdrawalAmount + "?");
                    Optional<ButtonType> result = confirmationAlert.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        double newBalance = currentBalance - withdrawalAmount;

                        updateBalance(connection, accountNumber, newBalance);

                        showAlert("Withdrawal successful. New balance: " + newBalance);
                        LastScene();
                    } else {
                        showAlert("Withdrawal canceled.");
                    }
                } else {
                    showAlert("Insufficient funds for withdrawal.");
                }
            } else {
                showAlert("Account not found.");
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            showAlert("Error processing withdrawal.");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void updateBalance(Connection connection, String accountNumber, double newBalance) throws SQLException {
        String updateQuery = "UPDATE balance_table_customerid SET Balance = ? WHERE CustomerID = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setDouble(1, newBalance);
            updateStatement.setString(2, accountNumber);
            updateStatement.executeUpdate();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void LastScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CollectCash.fxml"));
        Parent roots = loader.load();

        WithdrawalController cardInformationController = loader.getController();

        stage = (Stage) AccountNo.getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();
    }

    public void GoBackTowithdraw(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Withdrawal.fxml"));
        Parent roots = loader.load();
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();
    }

    public void Exitout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/First_Scene.fxml"));
        Parent roots = loader.load();
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
