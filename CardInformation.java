package com.example.atm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class CardInformation  implements Initializable {
    @FXML
    private TextField Pin_Number, Card_Number;
    @FXML
    public ComboBox<String> CardTypes;
    @FXML
    private Label setAmount;
    Scene scene;
    Stage stage;

    private String url ;
    private String username;
    private String password;



    public void saveToDatabase() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try{
            connection = DriverManager.getConnection(url, username, password);
            String cardType = CardTypes.getValue();
            String atmPin = Pin_Number.getText();
            String frontCard = Card_Number.getText();
            String CustomerID = setAmount.getText();



            String infocardinfo = "INSERT INTO card_information (CustomerID, atmPin, cardType, frontCard)VALUES(?,?,?,?)";

            preparedStatement = connection.prepareStatement(infocardinfo);
            preparedStatement.setString(1, CustomerID);
            preparedStatement.setString(2, atmPin);
            preparedStatement.setString(3, cardType);
            preparedStatement.setString(4, frontCard);




            System.out.println("customerId: " +setAmount);
            System.out.println("enteredPin: " + atmPin);
            System.out.println("selectedCardType: " + cardType);
            System.out.println("frontCardNumbers: " + frontCard);


            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully.");
            } else {
                System.out.println("Data not inserted.");
            }



        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }


    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDatabaseProperties();
        CardTypes.getItems().addAll("MasterCard", "Visa Card", "Verve Card");
        String ran = generateRandomNumbers();
        String splitCardNo = splitCardNumber(ran);
        Card_Number.setText(splitCardNo);
        Card_Number.setFont(new javafx.scene.text.Font(20));
        Card_Number.setStyle("-fx-text-fill: red;");

        TextFormatter<Integer> textFormatter = new TextFormatter<>(
                new IntegerStringConverter(),
                null,
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.length() > 4) {
                        return null;
                    }
                    return change;
                }
        );

        Pin_Number.setTextFormatter(textFormatter);
        Pin_Number.setFont(new javafx.scene.text.Font(20));
        Pin_Number.setStyle("-fx-text-fill: red;");
    }

    private String generateRandomNumbers() {
        Random random = new Random();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            result.append(random.nextInt(10));
        }

        return result.toString();
    }

    @FXML
    public void HaveACard(ActionEvent event) throws SQLException {
        saveToDatabase();
        if (!Pin_Number.getText().matches("\\d{4}")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Invalid Pin Number. It must be exactly 4 digits.");
            alert.show();
        }else {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/DisplayAtm.fxml"));
                Parent roots = loader.load();

                DisplayATmshow displayATmshow = loader.getController();
                String Number = Card_Number.getText();
                String Cardtype = CardTypes.getValue();
                displayATmshow.displayonCard(Number);
                displayATmshow.displayCardType(Cardtype);

                stage = (Stage) Card_Number.getScene().getWindow();
                scene = new Scene(roots);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();


            }

        }
    }
    public void displayName(String AccountNo) {
        setAmount.setText(AccountNo);
        setAmount.setFont(Font.font("Verdana", FontWeight.BOLD,17));


    }
    public static String splitCardNumber(String ran) {
        StringBuilder formattedNumber = new StringBuilder();

        for (int i = 0; i < ran.length(); i += 4) {
            int endIndex = Math.min(i + 4, ran.length());
            String group = ran.substring(i, endIndex);
            formattedNumber.append(group).append(" ");
        }

        return formattedNumber.toString().trim();
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





