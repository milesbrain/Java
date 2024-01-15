package com.example.atm;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;


public class Receipt{
    @FXML
    Label SenderAccNo, ReferenceNo, SenderName, ReceiverName;
    @FXML

    Label Amount;
    @FXML
    Label Date;
    @FXML
    Label ReceiverAccNo;
    public void returntomain(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/First_Scene.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("ATM");
        stage.setScene(scene);
        stage.show();
    }
}
