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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class DepositController extends confirmcontroller implements Initializable {
    private String url;
    private String username;
    private String password;

    @FXML
    private Button one, two, three, four, button5, button6, button7, button8, button9, zero, exitbutton, depositbutton;
    Stage stage;
    Scene scene;


    @FXML
    private TextField AmountEnter;

    private String formattedAmount;
    private NumberFormat currencyFormat;
    private String customerID;
    private String[] customerName;
    TextFormatter<Number> textFormatter;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDatabaseProperties();
        clickButton();
        addAmountFormatter();

    }
    private void addAmountFormatter() {

        AmountEnter.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {

                formatCurrency(newValue);
            }
        });
    }

    private void formatCurrency(String value) {
        if (!value.isEmpty()) {
            try {

                String numericValue = value.replaceAll("[^\\d.]", "");


                double amount = Double.parseDouble(numericValue);

                currencyFormat = NumberFormat.getCurrencyInstance();
                currencyFormat.setCurrency(Currency.getInstance("NGN"));
                formattedAmount = currencyFormat.format(amount);


                NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                String formattedNumber = numberFormat.format(amount);
                AmountEnter.setText(formattedNumber);
            } catch (NumberFormatException e) {

                e.printStackTrace();
            }
        }
    }
    private void insertNewTable(String customerID) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String insertQuery = "INSERT INTO balance_table_customerid (CustomerID, balance) VALUES (?, 0)";
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                insertStatement.setString(1, customerID);
                insertStatement.executeUpdate();
                System.out.println("Row for customer ID " + customerID + " created in balance_table.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    public void clickButton() {
        for (Button button : new Button[]{
                one, two, three, four, button5, button6, button7, button8, button9, zero
        }) {
            button.setOnAction(event -> handleNumericButtonClick(button.getText()));
        }


    }

    private void handleNumericButtonClick(String value) {
        AmountEnter.appendText(value);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    public void setExitbutton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DepositAccount.fxml"));
        Parent roots = loader.load();

        confirmcontroller loaderControllerontroller = loader.getController();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();
    }

    public void depositbutton(ActionEvent event) throws IOException {
        insertNewTable(customerID);

        String enteredAmount = AmountEnter.getText();
        if (enteredAmount.isEmpty()) {
            showAlert("Please enter the deposit amount.");
            return;
        }


        String numericPart = enteredAmount.replaceAll("[^\\d.]", "");

        try {
            double amount = Double.parseDouble(numericPart);



            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                String updateQuery = "UPDATE balance_table_customerid SET balance = balance + ? WHERE CustomerID = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setDouble(1, amount);
                    updateStatement.setString(2, customerID);
                    updateStatement.executeUpdate();
                }

                showAlert("Deposit successful. Amount: " + formattedAmount);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error occurred during deposit. Please try again.");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showAlert("Error occurred during deposit. Please try again.");
        }


        exittoMenu(event);
    }

    public void clearTextField(ActionEvent event) {
        AmountEnter.clear();
    }



    public void setCustomerInfo(String customerID, String[] customerName) {
        this.customerID = customerID;
        this.customerName = customerName;
    }



    public void exittoMenu(ActionEvent event) throws IOException {
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
