package com.bachulun.Controllers;

import java.io.IOException;

import com.bachulun.DAOs.UserDAO;
import com.bachulun.Models.User;
import com.bachulun.Service.IUserService;
import com.bachulun.Service.UserService;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final IUserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        try {
            User user = userService.loginUser(username, password);
            errorLabel.setText("Login successful!");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Expense Tracker");
            stage.show();
        } catch (InvalidInputException e) {
            errorLabel.setText(e.getMessage());
        } catch (DatabaseException e) {
            errorLabel.setText("Database error occurred");
        } catch (IOException e) {
            errorLabel.setText("Failed to load dashboard");
        }
    }

    @FXML
    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Failed to load registration screen");
        }
    }
}
