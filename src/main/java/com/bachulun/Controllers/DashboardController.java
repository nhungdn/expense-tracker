package com.bachulun.Controllers;

import com.bachulun.Models.Account;
import com.bachulun.Models.Transaction;
import com.bachulun.Models.User;
import com.bachulun.Service.AccountService;
import com.bachulun.Service.CategoryService;
import com.bachulun.Service.IAccountService;
import com.bachulun.Service.ICategoryService;
import com.bachulun.Service.ITransactionService;
import com.bachulun.Service.TransactionService;
import com.bachulun.Utils.DatabaseException;
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
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardController {

    private List<String> monthNames = Arrays.asList(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
            "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
            "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12");
    private final ITransactionService tranService = new TransactionService();
    private final IAccountService accountService = new AccountService();
    private final ICategoryService categoryService = new CategoryService();
    private MenuController menuController;
    private User currentUser;

    // Bar Chart
    @FXML
    private ComboBox<Integer> yearComboBoxBC;
    @FXML
    private BarChart<String, Double> incomeExpenseChart;

    // Pie Chart Chi
    @FXML
    private ComboBox<Integer> yearComboBoxPC;
    @FXML
    private ComboBox<String> monthComboBoxPC;
    @FXML
    PieChart categoryPieChart;

    // Pie Chart Thu
    @FXML
    private ComboBox<Integer> yearComboBoxPC1;
    @FXML
    private ComboBox<String> monthComboBoxPC1;
    @FXML
    PieChart categoryPieChart1;

    // Account List
    @FXML
    private ScrollPane accountListScrollPane;
    @FXML
    private HBox accountsBox;
    @FXML
    VBox totalMoneyCard;
    @FXML
    Button leftButton, rightButton;

    // Transaction
    @FXML
    VBox transactionContainer;
    @FXML
    Button viewAllTransaction;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getLoggedInUser();

        // Cac bang trong Dashboard
        AccountList();
        BarChart();
        PieChartExpense();
        PieChartIncome();
        recentTransaction();

        // Cuon trong accountList
        leftButton.setOnAction(e -> scrollHorizontally(-0.2));
        rightButton.setOnAction(e -> scrollHorizontally(0.2));

        // Navigate
        viewAllTransaction.setOnAction(e -> {
            menuController.viewAllTransaction();
        });
    }

    // AccountList
    // ----------------------------------------------------------------------------------------------------------------
    public void AccountList() {
        // Danh sach cac tai khoan
        List<Account> accountList = new ArrayList<>();
        try {
            accountList = accountService.getAccountsByUserId(currentUser.getId());
        } catch (Exception e) {
            System.err.println("Error when getting users accounts: " + e);
        }

        accountsBox.getChildren().clear();
        accountsBox.setSpacing(15);
        accountsBox.setFillHeight(false);
        accountsBox.setMinWidth(Region.USE_PREF_SIZE);

        // Tong tai san
        Double total = 0.0;

        for (Account acc : accountList) {
            total += acc.getBalance();

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
            Label money = new Label(String.format("%,.0f", acc.getBalance()) + " VND");

            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f9f9f9);"
                            + "-fx-background-radius: 15;"
                            + "-fx-border-radius: 15;"
                            + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0, 0, 3);");

            accountName.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-font-weight: bold;");
            if (acc.getBalance() > 0)
                money.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #5edc27ff;");
            else
                money.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e94444ff;");

            card.getChildren().addAll(accountName, money);

            // Hiệu ứng di chuột
            card.setOnMouseEntered(e -> {
                card.setScaleX(1.05);
                card.setScaleY(1.05);
                card.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #ffffffff, #8fd7f4ff);"
                                + "-fx-background-radius: 15;"
                                + "-fx-border-radius: 15;"
                                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0, 0, 3);");
            });

            card.setOnMouseExited(e -> {
                card.setScaleX(1);
                card.setScaleY(1);
                card.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f9f9f9);"
                                + "-fx-background-radius: 15;"
                                + "-fx-border-radius: 15;"
                                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0, 0, 3);");
            });

            // Thêm card
            accountsBox.getChildren().add(card);
        }

        // Chinh sua lai tong tai san
        totalMoneyCard.setSpacing(10);
        totalMoneyCard.setPadding(new Insets(15));

        totalMoneyCard.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label("Tổng tài sản");
        Label money = new Label(String.format("%,.0f", total) + " VND");

        totalMoneyCard.setStyle(
                "-fx-border-color: #007bff;" + "-fx-border-width: 3px;" + "-fx-border-radius: 15px;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0, 123, 255, 0.4), 20, 0.0, 0, 0);"
                        + "-fx-background-radius: 15px;"
                        + "-fx-background-color: #f0f8ff;");

        name.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-font-weight: bold;");
        if (total > 0)
            money.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #5edc27ff;");
        else
            money.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e94444ff;");

        totalMoneyCard.getChildren().addAll(name, money);

        accountListScrollPane.setFitToWidth(false);
        accountListScrollPane.setFitToHeight(false);
    }

    private void scrollHorizontally(double delta) {
        double newValue = accountListScrollPane.getHvalue() + delta;
        accountListScrollPane.setHvalue(Math.min(1.0, Math.max(0.0, newValue)));
    }

    // Bar Chart
    // -----------------------------------------------------------------------------------------------------
    public void BarChart() {
        // Chon nam
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 4; i <= currentYear + 1; i++) {
            yearComboBoxBC.getItems().add(i);
        }
        yearComboBoxBC.setValue(currentYear);

        // Load initial bar chart data
        try {
            loadBarChart();
        } catch (Exception e) {
            System.err.println("Error when loadBarChart: " + e.getMessage());
        }

        // Change BarChart data when year selection changes
        yearComboBoxBC.setOnAction(e -> {
            try {
                loadBarChart();
            } catch (Exception ex) {
                System.err.println("Error updating bar chart: " + ex.getMessage());
            }
        });
    }

    public void loadBarChart() throws Exception {
        incomeExpenseChart.getData().clear();

        Map<String, Double> incomeData = tranService.getMonthlyTotalsByTypeAndYear(currentUser.getId(), "Thu",
                yearComboBoxBC.getValue());
        Map<String, Double> expenseData = tranService.getMonthlyTotalsByTypeAndYear(currentUser.getId(), "Chi",
                yearComboBoxBC.getValue());

        XYChart.Series<String, Double> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Thu");
        XYChart.Series<String, Double> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Chi");

        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i);
            incomeSeries.getData().add(new XYChart.Data<>(month, incomeData.getOrDefault(month, 0.0)));
            expenseSeries.getData().add(new XYChart.Data<>(month, expenseData.getOrDefault(month, 0.0)));
        }

        incomeExpenseChart.getData().addAll(incomeSeries, expenseSeries);

        // Gán màu cột
        setBarColor(incomeSeries, "#00ff05");
        setBarColor(expenseSeries, "#e02c2c");

        for (XYChart.Series<String, Double> series : incomeExpenseChart.getData()) {
            // Sử dụng Platform.runLater để đảm bảo Node đã được tạo
            Platform.runLater(() -> {
                for (XYChart.Data<String, Double> data : series.getData()) {
                    // Kiểm tra xem Node của cột đã được tạo chưa
                    if (data.getNode() != null) {
                        double value = data.getYValue();
                        String seriesName = series.getName();
                        String month = data.getXValue();

                        // Định dạng giá trị tiền tệ
                        DecimalFormat df = new DecimalFormat("#,###.##");
                        String formattedValue = df.format(value) + " VNĐ";

                        // Tạo và tùy chỉnh Tooltip
                        Tooltip tooltip = new Tooltip(seriesName + " T" + month + ": " + formattedValue);
                        tooltip.setStyle(
                                "-fx-background-color: #0b3d91; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-size: 13px; " +
                                        "-fx-padding: 6px 10px; " +
                                        "-fx-background-radius: 8px;");

                        tooltip.setShowDelay(Duration.ZERO);
                        Tooltip.install(data.getNode(), tooltip);

                        // Thêm hiệu ứng khi di chuột
                        data.getNode().setOnMouseEntered(e -> {
                            data.getNode().setScaleX(1.05);
                            data.getNode().setScaleY(1.05);
                            // Chỉ áp dụng DropShadow nếu giá trị > 0 để tránh hiệu ứng trên cột rỗng
                            if (value > 0) {
                                data.getNode().setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.3)));
                            }
                        });
                        data.getNode().setOnMouseExited(e -> {
                            data.getNode().setScaleX(1);
                            data.getNode().setScaleY(1);
                            data.getNode().setEffect(null);
                        });
                    }
                }
            });
        }

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

    // Pie Chart
    // ---------------------------------------------------------------------------------------------------------------------------------
    public void PieChartExpense() {
        categoryPieChart.setLegendSide(javafx.geometry.Side.RIGHT);
        // Chon nam
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 4; i <= currentYear + 1; i++) {
            yearComboBoxPC.getItems().add(i);
        }
        yearComboBoxPC.setValue(currentYear);

        // Chon thang
        monthComboBoxPC.getItems().addAll(monthNames);
        int currentMonth = LocalDate.now().getMonthValue();
        monthComboBoxPC.setValue(monthNames.get(currentMonth - 1));

        // Load data
        try {
            loadPieChart(monthNames.indexOf(monthComboBoxPC.getValue()) + 1, yearComboBoxPC.getValue());
        } catch (Exception e) {
            System.err.println("Error when loadPieChartExpense: " + e.getMessage());
        }

        // Change PieChart data when year selection changes
        yearComboBoxPC.setOnAction(e -> {
            try {
                loadPieChart(monthNames.indexOf(monthComboBoxPC.getValue()) + 1, yearComboBoxPC.getValue());
            } catch (Exception ex) {
                System.err.println("Error updating pie chart: " + ex.getMessage());
            }
        });

        monthComboBoxPC.setOnAction(e -> {
            try {
                loadPieChart(monthNames.indexOf(monthComboBoxPC.getValue()) + 1, yearComboBoxPC.getValue());
            } catch (Exception ex) {
                System.err.println("Error updating pie chart: " + ex.getMessage());
            }
        });
    }

    public void loadPieChart(int month, int year) throws Exception {

        Map<Integer, Double> categoryTotals = tranService.getCategoryTotalsForMonth(month, "Chi", year);

        // Chuẩn bị dữ liệu cho PieChart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
            String categoryName = categoryService.getCategoryNameByCategoryId(entry.getKey());
            pieChartData.add(new PieChart.Data(categoryName, entry.getValue()));
        }

        // Cập nhật PieChart
        categoryPieChart.setData(pieChartData);

        // Tính tổng để quy đổi phần trăm
        double total = pieChartData.stream().mapToDouble(PieChart.Data::getPieValue).sum();

        for (PieChart.Data data : categoryPieChart.getData()) {
            double percent = (data.getPieValue() / total) * 100;
            String tooltipText = String.format("%s: %.1f%%", data.getName(), percent);

            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setStyle(
                    "-fx-background-color: #0b3d91; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 6px 10px; " +
                            "-fx-background-radius: 8px;");

            tooltip.setShowDelay(Duration.ZERO);
            Tooltip.install(data.getNode(), tooltip);

            // Hiệu ứng
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setScaleX(1.05);
                data.getNode().setScaleY(1.05);
                data.getNode().setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.3)));
            });
            data.getNode().setOnMouseExited(e -> {
                data.getNode().setScaleX(1);
                data.getNode().setScaleY(1);
                data.getNode().setEffect(null);
            });
        }
    }

    // Pie Chart Thu
    // -----------------------------------------------------------------------

    public void PieChartIncome() {
        categoryPieChart1.setLegendSide(Side.RIGHT);

        // Chon nam
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 4; i <= currentYear + 1; i++) {
            yearComboBoxPC1.getItems().add(i);
        }
        yearComboBoxPC1.setValue(currentYear);

        // Chon thang
        monthComboBoxPC1.getItems().addAll(monthNames);
        int currentMonth = LocalDate.now().getMonthValue();
        monthComboBoxPC1.setValue(monthNames.get(currentMonth - 1));

        // load data
        try {
            loadPieChartIncome(monthNames.indexOf(monthComboBoxPC1.getValue()) + 1, yearComboBoxPC1.getValue());
        } catch (Exception e) {
            System.err.println("Error when loadPieChartIncome: " + e);
        }

        yearComboBoxPC1.setOnAction(e -> {
            try {
                loadPieChartIncome(monthNames.indexOf(monthComboBoxPC1.getValue()) + 1, yearComboBoxPC1.getValue());
            } catch (Exception er) {
                System.err.println("Error when update PieChartIncome: " + er);
            }
        });

        monthComboBoxPC1.setOnAction(e -> {
            try {
                loadPieChartIncome(monthNames.indexOf(monthComboBoxPC1.getValue()) + 1, yearComboBoxPC1.getValue());
            } catch (Exception er) {
                System.err.println("Error when update PieChartIncome: " + er);
            }
        });
    }

    public void loadPieChartIncome(int month, int year) throws DatabaseException {
        Map<Integer, Double> categoryTotals = tranService.getCategoryTotalsForMonth(month, "Thu", year);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
            String cateName = categoryService.getCategoryNameByCategoryId(entry.getKey());
            pieChartData.add(new PieChart.Data(cateName, entry.getValue()));
        }

        // Cap nhat lai du lieu cho Pie Chart
        categoryPieChart1.setData(pieChartData);

        // Phan hien thi phan tram khi di chuyen chuot toi
        double total = pieChartData.stream().mapToDouble(PieChart.Data::getPieValue).sum();

        for (PieChart.Data data : categoryPieChart1.getData()) {
            double percent = (data.getPieValue() / total) * 100;
            String tooltipText = String.format("%s: %.1f%%", data.getName(), percent);

            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setStyle(
                    "-fx-background-color: #0b3d91; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 6px 10px; " +
                            "-fx-background-radius: 8px;");

            tooltip.setShowDelay(Duration.ZERO);
            Tooltip.install(data.getNode(), tooltip);

            // Hiệu ứng
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setScaleX(1.05);
                data.getNode().setScaleY(1.05);
                data.getNode().setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.3)));
            });
            data.getNode().setOnMouseExited(e -> {
                data.getNode().setScaleX(1);
                data.getNode().setScaleY(1);
                data.getNode().setEffect(null);
            });

        }
    }

    // Recent Transaction
    // ------------------------------------------------------------------------

    public void recentTransaction() {
        List<Transaction> tranList = new ArrayList<>();

        try {
            tranList = tranService.getLatestTransactions(currentUser.getId(), 5);
        } catch (DatabaseException e) {
            System.err.println(e);
        }

        for (Transaction t : tranList) {
            HBox card = new HBox();
            card.setPadding(new Insets(10));
            card.setSpacing(10);
            card.setPrefHeight(70);
            card.setAlignment(Pos.CENTER);
            card.setStyle(
                    "-fx-background-radius: 10; -fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);");

            VBox left = new VBox();
            String categoryName = "";
            try {
                categoryName = categoryService.getCategoryNameByCategoryId(t.getCategoryId());
            } catch (DatabaseException e) {
                System.err.println(e);
            }

            Label title = new Label(
                    categoryName + " - " + t.getDescription());
            title.setFont(Font.font(14));

            Label sub = new Label(t.getTransactionDateDisplay());
            sub.setTextFill(Color.GRAY);
            sub.setFont(Font.font(12));

            left.getChildren().addAll(title, sub);

            Label amount = new Label(String.format("%,d VND", (long) t.getAmount()));
            amount.setFont(Font.font(14));
            if (t.getType().equals("Chi")) {
                amount.setTextFill(Color.RED);
                card.setStyle("-fx-background-color: #ffe6e6; -fx-background-radius: 10;");
            } else {
                amount.setTextFill(Color.GREEN);
                card.setStyle("-fx-background-color: #e6f9e6; -fx-background-radius: 10;");
            }

            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);

            card.getChildren().addAll(left, space, amount);

            transactionContainer.getChildren().add(card);
        }
    }

    public void setMenuController(MenuController controller) {
        this.menuController = controller;
    }

}