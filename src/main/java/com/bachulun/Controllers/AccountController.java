package com.bachulun.Controllers;

import java.time.LocalDateTime;
import java.util.Optional;

import com.bachulun.Models.Account;
import com.bachulun.Models.User;
import com.bachulun.Service.AccountService;
import com.bachulun.Service.IAccountService;
import com.bachulun.Utils.SessionManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class AccountController {

    private User user;
    private ObservableList<Account> accountList = FXCollections.observableArrayList();
    private final IAccountService accountService = new AccountService();
    private static final int ROWS_PER_PAGE = 10;

    @FXML private Button addAccount;
    @FXML private TextField searchField, accountTextField;
    @FXML private javafx.scene.control.Label errorLabel;
    @FXML private TableView<Account> accounTableView;
    @FXML private TableColumn<Account, Number> numCol;
    @FXML private TableColumn<Account, String> accountNameCol, amountCol;
    @FXML private TableColumn<Account, Void> actionCol;
    @FXML private Pagination pagination;

    @FXML
    private void initialize() {
        user = SessionManager.getInstance().getLoggedInUser();

        accounTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        actionCol.setPrefWidth(150);

        numCol.setCellValueFactory(column -> {
            Account acc = column.getValue();
            return new SimpleIntegerProperty(accounTableView.getItems().indexOf(acc) + 1);
        });

        accountNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        loadAccountTable();
        addAccount.setOnAction(e -> handleAddAccount());
    }

    private void loadAccountTable() {
        try {
            accountList.clear();
            accountList.addAll(accountService.getAccountsByUserId(user.getId()));

            int pageCount = (int) Math.ceil((double) accountList.size() / ROWS_PER_PAGE);
            pagination.setPageCount(Math.max(pageCount, 1));

            accounTableView.setItems(accountList);
            addButtonToTable();
        } catch (Exception e) {
            System.err.println("Error when loadAccountTable: " + e.getMessage());
        }
    }

    private void addButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {

            private final Button detailBtn = new Button("Chi tiết");
            private final Button editBtn = new Button("Chỉnh Sửa");
            private final HBox actionBox = new HBox(5, detailBtn, editBtn);

            {
                actionBox.setStyle("-fx-alignment: center;");

                detailBtn.setMinWidth(65);
                editBtn.setMinWidth(65);

                detailBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                editBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");

                detailBtn.setOnAction(event -> {
                    Account account = getTableView().getItems().get(getIndex());
                    String info =
                        "Tên tài khoản: " + account.getName() +
                        "\nSố dư: " + account.getBalance() +
                        "\nNgày tạo: " + account.getCreatedAt();

                    showAlert("Chi tiết tài khoản", info, Alert.AlertType.INFORMATION);
                });

                editBtn.setOnAction(event -> {
                    Account account = getTableView().getItems().get(getIndex());
                    TextInputDialog dialog = new TextInputDialog(account.getName());
                    dialog.setTitle("Chỉnh sửa tài khoản");
                    dialog.setContentText("Tên mới:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(newName -> {
                        newName = newName.trim();
                        if (newName.isEmpty()) {
                            showAlert("Lỗi", "Tên tài khoản không được để trống!", Alert.AlertType.WARNING);
                            return;
                        }

                        try {
                            account.setName(newName);

                            try {
                                accountService.getClass()
                                    .getMethod("updateAccount", Account.class)
                                    .invoke(accountService, account);
                            } catch (NoSuchMethodException nsme) {
                                accountService.getClass()
                                    .getMethod("updateAccountName", int.class, String.class)
                                    .invoke(accountService, account.getId(), newName);
                            }

                            loadAccountTable();
                            showAlert("Thành công", "Đã cập nhật tên tài khoản!", Alert.AlertType.INFORMATION);
                        } catch (Exception ex) {
                            showAlert("Lỗi", ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void handleAddAccount() {
        String name = accountTextField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Tên tài khoản không được bỏ trống!");
            return;
        }

        try {
            accountService.addAccount(new Account(user.getId(), name, 0.0, LocalDateTime.now(), false));
            accountTextField.clear();
            errorLabel.setText("");
            loadAccountTable();
        } catch (Exception e) {
            System.err.println("Error when addAccount: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
