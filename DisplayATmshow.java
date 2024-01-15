package com.example.atm;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DisplayATmshow implements Initializable {
    @FXML
    Label CardTypeshow;
    @FXML
    Label CardNumbershows;
    Stage stage;
    Scene scene;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void displayonCard(String Number ){
        CardNumbershows.setText(Number);
        CardNumbershows.setFont(Font.font("Bell MT", FontWeight.BOLD,23));
        CardNumbershows.setTextFill(Color.GREEN);

    }
    public void displayCardType(String Cardtype){
        CardTypeshow.setText(Cardtype);
        CardTypeshow.setFont(Font.font("Bell MT", FontWeight.BOLD,21));

    }

    public void GoToMain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/First_Scene.fxml"));
        Parent roots = loader.load();




        stage = (Stage)CardTypeshow.getScene().getWindow();
        scene = new Scene(roots);
        stage.setScene(scene);
        stage.show();

    }
}