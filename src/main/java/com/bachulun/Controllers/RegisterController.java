package com.bachulun.Controllers;

import java.io.IOException;

import com.bachulun.DAOs.UserDAO;
import com.bachulun.Models.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty()) {
            showMessage("Email must not be empty.");
        } else if (username.isEmpty()) {
            showMessage("Username must not be empty.");
        } else if (password.isEmpty()) {
            showMessage("Password must not be empty.");
        } else if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.");
            return;
        }

        User user = new User(username, password, email, LocalDateTime.now());
        try {
            userDAO.registerUser(user);
            showMessage("Registration successful. You can now log in.");
        } catch (Exception e) {
            System.err.println("System error: " + e.getMessage());
        }
    }

    @FXML
    private void backToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
        }
    }

    private void showMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
