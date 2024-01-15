package com.example.atm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

public class  confirmcontroller implements Initializable {

    @FXML
    private Button one, two, three, four, button5, button6, button7, button8, button9, zero, exitbutton, depositbutton;
    @FXML
    private TextField AccountNo;
    @FXML
    private ComboBox<String> comboBox;
    Stage stage;
    Scene scene;
    String accountNumber;
    Alert  alert;

    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    private String url;
    private String username;
    private String password;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDatabaseProperties();
        clickButton();
        initializeComboBox();
    }

    public void clickButton() {
        for (Button button : new Button[]{
                one, two, three, four, button5, button6, button7, button8, button9, zero
        }) {
            button.setOnAction(event -> handleNumericButtonClick(button.getText()));
        }
    }

    private void handleNumericButtonClick(String value) {
        AccountNo.appendText(value);
        AccountNo.setFocusTraversable(true);
    }

    public void exitbutton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/First_Scene.fxml"));
        Parent roots = loader.load();

        Atm_controllr controller = loader.getController();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();
    }


    public void clear(ActionEvent event)
    {
        AccountNo.clear();
    }

    private boolean isValidAccountNumber(String accountNumber) {
        return accountNumber.matches("\\d{" + ACCOUNT_NUMBER_LENGTH + "}");
    }

    private void initializeComboBox() {
        ObservableList<String> combobox = FXCollections.observableArrayList("MILES BANK", "UBA BANK", "ZENITH BANK", "ACCESS BANK", "FIDELITY BANK");
        comboBox.setItems(combobox);
        TextFormatter<Integer> textFormatter = new TextFormatter<>(
                new IntegerStringConverter(),
                null,
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.length() > 10) {
                        return null;
                    }
                    return change;
                }
        );
        AccountNo.setTextFormatter(textFormatter);
    }


    public void confrim(ActionEvent event) throws IOException {
        accountNumber = AccountNo.getText();
        String selectedBank = comboBox.getValue();


        if (isValidAccountNumber(accountNumber) && "MILES BANK".equals(selectedBank)) {
            if (isAccountNumberExists(accountNumber)) {
                String[] customerInfo = getCustomerInfo(accountNumber);
                showConfirmationDialog(accountNumber, customerInfo, event);
            } else {
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Invalid Account Numnber");
                alert.showAndWait();
            }
        } else {
            if (accountNumber.isEmpty()|| selectedBank== null){
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("One of The field is empty");
                alert.showAndWait();
            }
            else{
                if(!selectedBank.equals("MILESBANK")){
                    System.out.println("No Service To This Bank No ");
                }
            }

        }
    }
    private boolean isAccountNumberExists(String accountNumber) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT COUNT(*) FROM registration WHERE CustomerID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, accountNumber);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() && resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String[] getCustomerInfo(String accountNumber) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = "SELECT First_Name, Surname FROM registration WHERE CustomerID = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, accountNumber);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String firstName = resultSet.getString("First_Name");
                        String surname = resultSet.getString("Surname");
                        return new String[]{firstName, surname};
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showConfirmationDialog(String CustomerID, String[] First_Name, ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deposit Confirmation");
        alert.setHeaderText("Do you want to deposit to the account?");
        alert.setContentText("Customer ID: " + CustomerID+ "\n" +
                "First Name: " + First_Name[0] + "\n" +
                "Surname: " + First_Name[1]);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/deposit.fxml"));
            Parent roots = loader.load();

            DepositController controller = loader.getController();
            controller.setCustomerInfo(CustomerID,First_Name);

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(roots);
            stage.setScene(scene);
            stage.show();


        } else {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("User clicked Cancel or closed the dialog.");
            alert.showAndWait();
        }
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


