package com.bachulun.Controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.bachulun.Models.User;
import com.bachulun.Utils.SessionManager;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MenuController {
    private boolean isMenuHidden;
    private User currentUser;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    BorderPane rootPane;
    // Top
    @FXML
    private Label welcomeLabel;
    @FXML
    Label timeLabel;
    @FXML
    ToggleButton showMenuButton;

    // Left - Menu bar
    @FXML
    private VBox menuBar;
    @FXML
    private Button dashBoardButton;
    @FXML
    private Button viewAllTransactionButton;
    @FXML
    private Button viewAllAccountsButton;
    @FXML
    private Button viewAllCategoriesButton;
    @FXML
    private Button settingButton;
    @FXML
    private Button logOutButton;

    @FXML
    private VBox menuBarShort;
    @FXML
    private Button dashBoardIconButton;
    @FXML
    private Button viewAllTransactionIconButton;
    @FXML
    private Button viewAllAccountsIconButton;
    @FXML
    private Button viewAllCategoriesIconButton;
    @FXML
    private Button settingIconButton;
    @FXML
    private Button logOutIconButton;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getLoggedInUser();

        isMenuHidden = false;
        welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");

        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, e -> timeLabel.setText(LocalDateTime.now().format(formatter))),
                new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Show or hide menu
        showMenuButton.setOnAction(e -> showMenu());

        // Navigate
        viewAllTransactionButton.setOnAction(e -> viewAllTransaction());
        viewAllAccountsButton.setOnAction(e -> viewAllAccounts());
        viewAllCategoriesButton.setOnAction(e -> viewAllCategories());
        dashBoardButton.setOnAction(e -> viewDashboard());
        settingButton.setOnAction(e -> viewSettings());
        logOutButton.setOnAction(e -> handleLogout());

        viewAllTransactionIconButton.setOnAction(e -> viewAllTransaction());
        viewAllAccountsIconButton.setOnAction(e -> viewAllAccounts());
        viewAllCategoriesIconButton.setOnAction(e -> viewAllCategories());
        dashBoardIconButton.setOnAction(e -> viewDashboard());
        settingIconButton.setOnAction(e -> viewSettings());
        logOutIconButton.setOnAction(e -> handleLogout());

    }

    private void showMenu() {
        if (!isMenuHidden) {
            // An menu
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), menuBar);
            slideOut.setFromX(0);
            slideOut.setToX(-menuBar.getWidth());

            slideOut.play();

            isMenuHidden = true;
        } else {
            menuBar.setTranslateX(-menuBar.getWidth());

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), menuBar);
            slideIn.setFromX(-menuBar.getWidth());
            slideIn.setToX(0);

            slideIn.play();

            isMenuHidden = false;
        }
    }

    // Navigate
    // ----------------------------------------------------------------------------------------
    public void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Login.fxml"));
            SessionManager.getInstance().logout();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setTitle("Expense Tracker");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error when log out: " + e.getMessage());
        }

    }

    private void viewDashboard() {
        navigateTo("/FXML/Dashboard.fxml");
        setActiveButton(dashBoardButton, dashBoardIconButton);
    }

    public void viewAllTransaction() {
        navigateTo("/FXML/Transaction.fxml");
        setActiveButton(viewAllTransactionButton, viewAllTransactionIconButton);
    }

    private void viewAllAccounts() {
        navigateTo("/FXML/Account.fxml");
        setActiveButton(viewAllAccountsButton, viewAllAccountsIconButton);
    }

    private void viewAllCategories() {
        navigateTo("/FXML/Category.fxml");
        setActiveButton(viewAllCategoriesButton, viewAllCategoriesIconButton);
    }

    private void viewSettings() {
        navigateTo("/FXML/Settings.fxml");
        setActiveButton(settingButton, settingIconButton);
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (fxmlPath.contains("Settings.fxml")) {
                SettingsController settingController = loader.getController();
                settingController.setMenuController(this);
            } else if (fxmlPath.contains("Dashboard.fxml")) {
                DashboardController dashboardController = loader.getController();
                dashboardController.setMenuController(this);
            }

            rootPane.setCenter(root);
        } catch (IOException e) {
            System.err.println("Error when navigate to " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeBtn, Button activeIconBtn) {
        dashBoardButton.getStyleClass().remove("active");
        viewAllAccountsButton.getStyleClass().remove("active");
        viewAllTransactionButton.getStyleClass().remove("active");
        viewAllCategoriesButton.getStyleClass().remove("active");
        settingButton.getStyleClass().remove("active");

        dashBoardIconButton.getStyleClass().remove("active");
        viewAllAccountsIconButton.getStyleClass().remove("active");
        viewAllCategoriesIconButton.getStyleClass().remove("active");
        viewAllTransactionIconButton.getStyleClass().remove("active");
        settingIconButton.getStyleClass().remove("active");

        if (!activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }

        if (!activeIconBtn.getStyleClass().contains("active")) {
            activeIconBtn.getStyleClass().add("active");
        }
    }

    public void setWelcomeLabel(String newName) {
        welcomeLabel.setText("Welcome, " + newName + "!");
    }

}
