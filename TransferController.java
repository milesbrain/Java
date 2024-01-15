package com.example.atm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class TransferController implements Initializable {
    @FXML
    ComboBox<String> combobox;
    @FXML
    ComboBox<String> combo;
    @FXML
    TextField AccountNoSender;
    @FXML
    TextField AmountSend;
    @FXML
    TextField RecipientAccNo;

    private String senderName;
    private String senderAccNo;
    private double transferAmount;
    private String receiverName;
    private String receiverAccNo;
    private String referenceNo;
    String formattedDate;
    String senderAccountNo;
    String recipientAccountNo;
    double amount;

    private String url;
    private String username;
    private String password;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDatabaseProperties();

        combobox.getItems().addAll("MILES BANK", "UBA BANK", "ACCESS BANK", "ZENITH BANK", "FIDELITY BANK");
        initializeCombo();
        setNumericTextFormatter(AccountNoSender);
        setNumericTextFormatter(AmountSend);
        setNumericTextFormatter(RecipientAccNo);
    }

    private void setNumericTextFormatter(TextField textField) {
        TextFormatter<Object> textFormatter = new TextFormatter<>(this::filterNumericInput);
        textField.setTextFormatter(textFormatter);
    }

    private TextFormatter.Change filterNumericInput(TextFormatter.Change change) {
        String newText = change.getControlNewText();
        if (newText.matches("\\d*")) {
            return change;
        } else {
            return null;
        }
    }

    public void initializeCombo() {
        combo.getItems().addAll("MILES BANK", "UBA BANK", "ACCESS BANK", "ZENITH BANK", "FIDELITY BANK");
    }

    public void sendmoney(ActionEvent event) {
        String senderBank = combobox.getValue();
        String recipientBank = combo.getValue();
        senderAccountNo = AccountNoSender.getText();
        recipientAccountNo = RecipientAccNo.getText();

        try {
            amount = Double.parseDouble(AmountSend.getText());
        } catch (NumberFormatException e) {
            showalert("Invalid amount. Please enter a valid number.");
            return;
        }

        senderName = getSenderName(senderAccountNo);
        senderAccNo = senderAccountNo;
        transferAmount = amount;

        try {
            if (recipientBank == null || senderBank == null) {
                showalert("Kindly input the bank");
            } else {
                if (recipientBank.equals("MILES BANK") && senderBank.equals("MILES BANK")) {
                    if (senderAccountNo.equals(recipientAccountNo)) {
                        showalert("Sender and Receiver account numbers cannot be the same.");
                    } else {
                        if (!(senderAccountNo == null) || (recipientAccountNo == null)) {
                            double senderbalance = getSenderBalance(senderAccountNo);

                            if (amount > senderbalance) {
                                showalert("Insufficient balance. Your current balance is: " + senderbalance);

                                returntomain(event);
                                return;

                            }

                            if (transferMoney(senderAccountNo, recipientAccountNo, amount)) {
                                System.out.println("Transfer successful!");
                                showalert("Transfer successful!");
                                receiverName = getReceiverName(recipientAccountNo);
                                receiverAccNo = recipientAccountNo;

                                referenceNo = generateReferenceNumber();
                                LocalDate currentDate = LocalDate.now();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                formattedDate = LocalDateTime.now().format(formatter);

                                openReceiptScene(formattedDate);

                            } else {
                                showalert("Transfer failed");
                            }
                        } else {
                            showalert("Kindly Insert Your Information");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean transferMoney(String senderAccountNo, String recipientAccountNo, double amount) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            if (!accountExists(connection, recipientAccountNo)) {
                showalert("Recipient's account not found. Transfer failed.");
                return false;
            }

            String updateSenderQuery = "UPDATE balance_table_customerid SET balance = balance - ? WHERE CustomerID = ?";
            String updateRecipientQuery = "UPDATE balance_table_customerid SET balance = balance + ? WHERE CustomerID = ?";

            try (PreparedStatement updateSenderStmt = connection.prepareStatement(updateSenderQuery);
                 PreparedStatement updateRecipientStmt = connection.prepareStatement(updateRecipientQuery)) {

                connection.setAutoCommit(false);

                updateSenderStmt.setDouble(1, amount);
                updateSenderStmt.setString(2, senderAccountNo);
                updateSenderStmt.executeUpdate();

                updateRecipientStmt.setDouble(1, amount);
                updateRecipientStmt.setString(2, recipientAccountNo);
                updateRecipientStmt.executeUpdate();

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean accountExists(Connection connection, String accountNo) throws SQLException {
        String query = "SELECT COUNT(*) FROM balance_table_customerid WHERE CustomerID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNo);
            try (ResultSet resultSet = stmt.executeQuery()) {
                resultSet.next();
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
    }

    private String getSenderName(String accountNo) {
        String query = "SELECT Surname,First_Name FROM registration WHERE CustomerID = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNo);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("Surname") + " " + resultSet.getString("First_Name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getSenderBalance(String accountNo) throws IOException {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String query = "SELECT balance FROM balance_table_customerid WHERE CustomerID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, accountNo);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getDouble("balance");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    private String getReceiverName(String accountNo) {
        String query = "SELECT Surname,First_Name FROM registration WHERE CustomerID = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNo);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("Surname") + " " + resultSet.getString("First_Name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateReferenceNumber() {
        Random random = new Random();
        StringBuilder referenceNumber = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            referenceNumber.append(random.nextInt(15));
        }
        return referenceNumber.toString();
    }

    private void openReceiptScene(String formattedDate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OpenAnotherScene.fxml"));
            Parent root = loader.load();

            Receipt receiptController = loader.getController();

            receiptController.SenderName.setText(senderName);
            receiptController.SenderAccNo.setText(senderAccNo);
            receiptController.Amount.setText(String.valueOf(transferAmount));
            receiptController.ReceiverName.setText(receiverName);
            receiptController.ReceiverAccNo.setText(receiverAccNo);
            receiptController.ReferenceNo.setText(referenceNo);
            receiptController.Date.setText(formattedDate);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.getIcons().add(new Image("myAtmLogo.png"));
            stage.setTitle("Receipt");
            stage.show();
            Stage currentStage = (Stage) combobox.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean showalert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        return false;
    }

    public void returntomain(ActionEvent event) throws IOException {
        FXMLLoader root = new FXMLLoader(getClass().getResource("/First_Scene.fxml"));
        Parent load = root.load();
        Scene scene = new Scene(load);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
