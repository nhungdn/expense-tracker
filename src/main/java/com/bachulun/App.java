package com.bachulun;

import com.bachulun.Utils.DatabaseUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        DatabaseUtil.initDatabase();

        Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Login.fxml"));
        primaryStage.setTitle("Expense Tracker");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}