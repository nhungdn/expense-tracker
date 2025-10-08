package com.bachulun.Controllers;

import com.bachulun.DAOs.UserDAO;
import com.bachulun.Models.User;

import java.sql.SQLException;
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

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User user = userDAO.loginUser(username, password);
            if (user != null) {
                errorLabel.setVisible(true);
                errorLabel.setText("Login successful." + user.getUsername());
            } else {
                errorLabel.setVisible(true);
                errorLabel.setText("Username or Password is wrong.\n Don't have an account? Register now!");
            }
        } catch (SQLException e) {
            System.err.println("System error: " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println("Error opening registration screen: " + e.getMessage());
        }
    }
}
