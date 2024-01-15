package com.example.atm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Registration extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {


            Parent root = FXMLLoader.load(getClass().getResource("/First_Scene.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setTitle("ATM");
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image("myAtmLogo.png"));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
