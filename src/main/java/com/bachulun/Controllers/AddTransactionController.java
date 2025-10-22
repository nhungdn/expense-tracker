package com.bachulun.Controllers;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

import com.bachulun.DAOs.AccountDAO;
import com.bachulun.DAOs.CategoryDAO;
import com.bachulun.DAOs.TransactionDAO;
import com.bachulun.Models.Transaction;
import com.bachulun.Models.User;
import com.bachulun.Service.AccountService;
import com.bachulun.Service.CategoryService;
import com.bachulun.Service.IAccountService;
import com.bachulun.Service.ICategoryService;
import com.bachulun.Service.ITransactionService;
import com.bachulun.Service.TransactionService;
import com.bachulun.Utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AddTransactionController {

    private User currentUser;
    private Map<String, Integer> accountMap = new LinkedHashMap<>();
    private Map<String, Integer> categoryMap = new LinkedHashMap<>();
    private final ITransactionService tranService = new TransactionService();
    private final IAccountService accountService = new AccountService();
    private final ICategoryService cateService = new CategoryService();

    @FXML
    private TextField amountTf;
    @FXML
    private DatePicker transactionDate;
    @FXML
    private TextField descriptionTf;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private ComboBox<String> accountComboBox;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    Button confirmButton;

    @FXML
    private Label amountError, tranDateError, descriptionError, accountError, categoryError, typeError;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getLoggedInUser();

        confirmButton.setOnAction(e -> addNewTransaction());

        try {
            accountMap = accountService.getAllAccountIdAndNameByUser(currentUser.getId());
            categoryMap = cateService.getAllCategoryIdAndNameByUserId(currentUser.getId());
        } catch (Exception e) {
            System.err.println(e);
        }

        loadAccountList(); // Set up cac Account
        loadCategoryList(); // Set up cac Category
        loadTypeList(); // Set up cac Type
    }

    private void loadAccountList() {
        List<String> accountList = accountMap.keySet().stream().collect(Collectors.toList());
        accountComboBox.setItems(FXCollections.observableArrayList(accountList));
    }

    private void loadCategoryList() {
        List<String> categoryList = categoryMap.keySet().stream().collect(Collectors.toList());
        categoryComboBox.setItems(FXCollections.observableArrayList(categoryList));
    }

    private void loadTypeList() {
        typeComboBox.setItems(FXCollections.observableArrayList("income", "expense"));
    }

    private void addNewTransaction() {
        String amountText = amountTf.getText();
        Double amount = 0.0;
        LocalDate tranDate = transactionDate.getValue();
        String description = descriptionTf.getText();
        String account = accountComboBox.getValue();
        String type = typeComboBox.getValue();
        String category = categoryComboBox.getValue();

        boolean mark = true;

        if (!amountText.trim().equals("")) {
            try {
                amount = Double.parseDouble(amountText);
                amountError.setText("Hợp lệ.");
                amountError.setStyle("-fx-text-fill: #008000;");
            } catch (NumberFormatException e) {
                amountError.setText("Số tiền không hợp lệ!");
                mark = false;
            }
        } else {
            amountError.setText("Số tiền không được trống!");
        }

        if (tranDate == null) {
            tranDateError.setText("Ngày giao dịch không được trống!");
            mark = false;
        } else {
            tranDateError.setText("Hợp lệ.");
            tranDateError.setStyle("-fx-text-fill: #008000;");
        }

        if (description.trim().equals("")) {
            descriptionError.setText("Mô tả không được bỏ trống!");
            mark = false;
        } else {
            descriptionError.setText("Hợp lệ.");
            descriptionError.setStyle("-fx-text-fill: #008000;");
        }

        if (account == null) {
            accountError.setText("Vui lòng chọn tài khoản.");
            mark = false;
        } else {
            accountError.setText("Hợp lệ.");
            accountError.setStyle("-fx-text-fill: #008000;");
        }

        if (type == null) {
            typeError.setText("Vui lòng chọn phân loại.");
            mark = false;
        } else {
            typeError.setText("Hợp lệ.");
            typeError.setStyle("-fx-text-fill: #008000;");
        }

        if (category == null) {
            categoryError.setText("Vui lòng chọn danh mục.");
            mark = false;
        } else {
            categoryError.setText("Hợp lệ.");
            categoryError.setStyle("-fx-text-fill: #008000;");
        }

        if (mark == false)
            return;

        Transaction tran = new Transaction(accountMap.get(account), categoryMap.get(category), amount, type,
                description, tranDate.atStartOfDay(), LocalDateTime.now());
        try {
            tranService.addTransaction(tran);

            // Thong bao tao thanh cong
            Label tbao = new Label("Bạn đã tạo giao dịch thành công.");
            Button closeButton = new Button("Đóng");

            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            vbox.setSpacing(10);
            vbox.getChildren().addAll(tbao, closeButton);
            StackPane stackPane = new StackPane(vbox);

            Scene scene = new Scene(stackPane, 200, 100);
            Stage stage = (Stage) amountTf.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Thông báo");
            stage.show();

            closeButton.setOnAction(e -> stage.close());

        } catch (Exception e) {
            System.err.println("Error when insert transaction into DB: " + e.getMessage());
        }
    }
}
