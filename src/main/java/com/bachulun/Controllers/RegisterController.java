package com.bachulun.Controllers;

import java.io.IOException;

import com.bachulun.Models.User;
import com.bachulun.Service.IUserService;
import com.bachulun.Service.UserService;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import java.time.LocalDateTime;

public class RegisterController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;

    private final IUserService userService = new UserService();

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!confirmPassword.equals(password)) {
            errorLabel.setText("Mat khau khong khop!");
            return;
        }

        User user = new User(username, password, email, LocalDateTime.now());
        try {
            userService.registerUser(user);
            errorLabel.setText("Registration successful!");

            Parent login = FXMLLoader.load(getClass().getResource("/Fxml/Login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Expense Tracker");
            stage.setScene(new Scene(login));
            stage.show();

        } catch (InvalidInputException e) {
            errorLabel.setText(e.getMessage());
        } catch (DatabaseException e) {
            errorLabel.setText("Database error occurred");
        } catch (IOException e) {
            errorLabel.setText("Failed to load login screen");
        }
    }

    @FXML
    private void backToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Expense Tracker");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e);
        }
    }
}
