package com.bachulun.Controllers;

import com.bachulun.DAOs.AccountDAO;
import com.bachulun.DAOs.ITransactionDAO;
import com.bachulun.DAOs.TransactionDAO;
import com.bachulun.Models.Account;
import com.bachulun.Models.User;
import com.bachulun.Service.AccountService;
import com.bachulun.Service.CategoryService;
import com.bachulun.Service.IAccountService;
import com.bachulun.Service.ICategoryService;
import com.bachulun.Service.ITransactionService;
import com.bachulun.Service.TransactionService;
import com.bachulun.Utils.SessionManager;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    private boolean isMenuHidden = false;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final ITransactionService tranService = new TransactionService();
    private final IAccountService accountService = new AccountService();
    private final ICategoryService categoryService = new CategoryService();
    private User currentUser;

    @FXML
    BorderPane rootPane;
    // Top
    @FXML
    private Label usernameLabel;
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
    private Button logOutButton;

    // Center
    @FXML
    private ScrollPane centerPane;

    // Bar Chart
    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private BarChart<String, Double> incomeExpenseChart;

    // Account List
    @FXML
    private ScrollPane accountListScrollPane;
    @FXML
    private HBox accountsBox;

    // Transaction

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getLoggedInUser();

        // Some settings
        centerPane.setFitToWidth(true);
        centerPane.setFitToHeight(true);
        accountListScrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        accountListScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        accountListScrollPane.setFitToHeight(true);
        usernameLabel.setText("Welcome, " + currentUser.getUsername() + "!");

        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, e -> timeLabel.setText(LocalDateTime.now().format(formatter))),
                new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Cac bang trong Dashboard
        AccountList();
        BarChart();

        // Show or hide menu
        showMenuButton.setOnAction(e -> showMenu());

        // Navigate
        viewAllTransactionButton.setOnAction(e -> viewAllTransaction());
        viewAllAccountsButton.setOnAction(e -> viewAllAccounts());
        viewAllCategoriesButton.setOnAction(e -> viewAllCategories());
        dashBoardButton.setOnAction(e -> backToDashboard());
        logOutButton.setOnAction(e -> handleLogout());
    }

    public void showMenu() {
        if (!isMenuHidden) {
            // An menu
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), menuBar);
            slideOut.setFromX(0);
            slideOut.setToX(-menuBar.getWidth());

            FadeTransition ft = new FadeTransition(Duration.millis(200), menuBar);
            ft.setFromValue(1);
            ft.setToValue(0);

            slideOut.setOnFinished(e -> rootPane.setLeft(null));
            slideOut.play();
            ft.play();

            isMenuHidden = true;
        } else {
            rootPane.setLeft(menuBar); // Them lai menu vao layout
            menuBar.setTranslateX(-menuBar.getWidth());
            menuBar.setOpacity(0);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), menuBar);
            slideIn.setFromX(-menuBar.getWidth());
            slideIn.setToX(0);

            FadeTransition ft = new FadeTransition(Duration.millis(200), menuBar);
            ft.setFromValue(0);
            ft.setToValue(1);

            slideIn.play();
            ft.play();
            isMenuHidden = false;
        }
    }

    public void AccountList() {
        List<Account> accountList = new ArrayList<>();
        try {
            accountList = accountService.getAccountsByUserId(currentUser.getId());
        } catch (Exception e) {
            System.err.println("Error when getting users accounts: " + e);
        }

        for (Account acc : accountList) {
            VBox card = new VBox();
            card.setSpacing(10);
            card.setPadding(new Insets(15));
            card.setPrefSize(200, 100);
            card.setPrefWidth(200);
            card.setPrefHeight(120);
            card.setMinHeight(120);
            card.setMaxHeight(120);

            card.setAlignment(Pos.CENTER_LEFT);

            Label accountName = new Label(acc.getName());
            Label money = new Label("$" + String.format("%,.0f", acc.getBalance()));

            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f9f9f9);"
                            + "-fx-background-radius: 15;"
                            + "-fx-border-radius: 15;"
                            + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0, 0, 3);");

            accountName.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-font-weight: bold;");
            money.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #333;");

            card.getChildren().addAll(accountName, money);
            accountsBox.getChildren().add(card);
        }
    }

    public void BarChart() {
        // Initialize choice box with years and set current year as selected
        yearComboBox.getItems().addAll(2023, 2024, 2025, 2026);
        int currentYear = LocalDate.now().getYear();
        if (yearComboBox.getItems().contains(currentYear)) {
            yearComboBox.getSelectionModel().select(Integer.valueOf(currentYear));
        } else {
            yearComboBox.getSelectionModel().selectFirst();
        }

        // Load initial bar chart data
        try {
            loadBarChart();
        } catch (Exception e) {
            System.err.println("Error when loadBarChart: " + e.getMessage());
        }

        // Change BarChart data when year selection changes
        yearComboBox.setOnAction(e -> {
            try {
                loadBarChart();
            } catch (Exception ex) {
                System.err.println("Error updating bar chart: " + ex.getMessage());
            }
        });
    }

    public void loadBarChart() throws Exception {
        incomeExpenseChart.getData().clear();

        Map<String, Double> incomeData = tranService.getMonthlyTotalsByTypeAndYear(currentUser.getId(), "income",
                yearComboBox.getValue());
        Map<String, Double> expenseData = tranService.getMonthlyTotalsByTypeAndYear(currentUser.getId(), "expense",
                yearComboBox.getValue());

        XYChart.Series<String, Double> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        XYChart.Series<String, Double> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense");

        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i);
            incomeSeries.getData().add(new XYChart.Data<>(month, incomeData.getOrDefault(month, 0.0)));
            expenseSeries.getData().add(new XYChart.Data<>(month, expenseData.getOrDefault(month, 0.0)));
        }

        incomeExpenseChart.getData().addAll(incomeSeries, expenseSeries);

        // Gán màu cột
        setBarColor(incomeSeries, "#00ff05");
        setBarColor(expenseSeries, "#e02c2c");

        // Cập nhật màu legend
        updateLegendColors();
    }

    private void setBarColor(XYChart.Series<String, Double> series, String color) {
        // Cần đợi JavaFX render node xong mới chỉnh màu
        Platform.runLater(() -> {
            for (XYChart.Data<String, Double> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        });
    }

    private void updateLegendColors() {
        Platform.runLater(() -> {
            Node legend = incomeExpenseChart.lookup(".chart-legend");
            if (legend != null) {
                // Lấy tất cả item trong legend
                List<Node> legendItems = legend.lookupAll(".chart-legend-item-symbol").stream().toList();
                if (legendItems.size() >= 2) {
                    // Active User
                    legendItems.get(0).setStyle("-fx-background-color: #00ff05;");
                    // Inactive User
                    legendItems.get(1).setStyle("-fx-background-color: #e02c2c;");
                }
            }
        });
    }

    public void PieChart() {
        // Implementation for initializing pie chart
    }

    public void backToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Expense Tracker");
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load dashboard: " + e);
        }
    }

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

    public void viewAllTransaction() {
        navigateTo("/FXML/Transaction.fxml");
    }

    private void viewAllAccounts() {
        navigateTo("/FXML/Account.fxml");
    }

    private void viewAllCategories() {
        navigateTo("/FXML/Category.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.setCenter(root);
        } catch (IOException e) {
            System.err.println("Error when navigate to " + fxmlPath);
            e.printStackTrace();
        }
    }
}
