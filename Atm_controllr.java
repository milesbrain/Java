package com.example.atm;



import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;


public class Atm_controllr implements Initializable {
    Stage stage;
    Scene scene;
    @FXML
    public TextField customerId;

    @FXML
    public TextField emailTextField;
    @FXML
    public TextField first_nameTextField;
    @FXML
    public TextField homeAddressTextField;
    @FXML
    public TextField phoneNumberTextField;
    @FXML
    public TextField surnameTextField;
    @FXML
    public TextField OccupationTextField;
    @FXML
    private PasswordField passwordField;

    @FXML
    public void saveToDatabase() throws SQLException {
        Alert alert;

        String AccountNo = customerId.getText();
        customerId = new TextField();
        String Email = emailTextField.getText();
        String first_name = first_nameTextField.getText();
        String Address = homeAddressTextField.getText();
        String passwords = passwordField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        String surname = surnameTextField.getText();
        String Occupation = OccupationTextField.getText();

        if (AccountNo.isEmpty() || Email.isEmpty() ||
                first_name.isEmpty() || Address.isEmpty() ||
                passwords.isEmpty() || phoneNumber.isEmpty() ||
                surname.isEmpty() || Occupation.isEmpty()) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("One or More field is empty");
            alert.showAndWait();
            return;
        }

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find db.properties");
                return;
            }


            prop.load(input);

            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                String customerExistQuery = "SELECT * FROM registration WHERE CustomerID = ?";
                try (PreparedStatement customerExistStatement = connection.prepareStatement(customerExistQuery)) {
                    customerExistStatement.setString(1, AccountNo);
                    ResultSet customerExistResult = customerExistStatement.executeQuery();

                    if (customerExistResult.next()) {
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setContentText("CustomerID already exists!");
                        alert.showAndWait();
                        return;
                    }
                }

                String phoneNumberExistQuery = "SELECT * FROM registration WHERE phone_Number = ?";
                try (PreparedStatement phoneNumberExistStatement = connection.prepareStatement(phoneNumberExistQuery)) {
                    phoneNumberExistStatement.setString(1, phoneNumber);
                    ResultSet phoneNumberExistResult = phoneNumberExistStatement.executeQuery();

                    if (phoneNumberExistResult.next()) {
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setContentText("PhoneNumber already exists!");
                        alert.showAndWait();
                        return;
                    }
                }

                String insertQuery = "INSERT INTO registration (CustomerID, Email, First_Name, Home_Address, password, phone_Number, Surname, Occupation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, AccountNo);
                    preparedStatement.setString(2, Email);
                    preparedStatement.setString(3, first_name);
                    preparedStatement.setString(4, Address);
                    preparedStatement.setString(5, passwords);
                    preparedStatement.setString(6, phoneNumber);
                    preparedStatement.setString(7, surname);
                    preparedStatement.setString(8, Occupation);

                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("success");
                        alert.setContentText("Your Account has been created");
                        alert.showAndWait();
                        connection.close();

                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CardInformation.fxml"));
                            Parent roots = loader.load();

                            CardInformation cardInformationController = loader.getController();
                            cardInformationController.displayName(AccountNo);

                            stage = (Stage) customerId.getScene().getWindow();
                            scene = new Scene(roots);
                            stage.setScene(scene);
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Data not saved to the database successfully");
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }


    public void CreateNewAccountButton(ActionEvent event) throws IOException, SQLException {
        Parent root = FXMLLoader.load(getClass().getResource("/CreateNewAccount.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        saveToDatabase();


    }


    public void ChangePin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ChangePin.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public void BalanceButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Balacecheck.fxml"));
        Parent roots = loader.load();


        BalanceController controller = loader.getController();


        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void DepositButton(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DepositAccount.fxml"));
        Parent roots = loader.load();


        confirmcontroller controller = loader.getController();


        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();

    }

    public void withdrawalButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Withdrawal.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public void TransferButton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Transfer.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void initialize(URL location, ResourceBundle resources) {
        customerId.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,10}?")) {
                customerId.setText(oldValue);
            } else if (newValue.length() < 10) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("AccountNo must be 10 digits");
                alert.show();
            }
        });

        emailTextField.focusedProperty().addListener((arg0, oldValues, newValues) -> {
            if (!newValues) {
                if (!emailTextField.getText().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Invalid Email Address");
                    alert.show();
                }
            }
        });

        phoneNumberTextField.focusedProperty().addListener((arg, oldVal, newVal) -> {
            if (!newVal) {
                if (!phoneNumberTextField.getText().matches("\\d{11}")) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Invalid Phone Number");
                    alert.showAndWait();
                }
            }
        });
    }
}