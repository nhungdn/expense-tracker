package com.bachulun.Controllers;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class AccountController {

    private User user;
    private ObservableList<Account> accountList = FXCollections.observableArrayList();
    private final IAccountService accountService = new AccountService();
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    private Button addAccount;
    @FXML
    private TextField searchField, accountTextField;
    @FXML
    private javafx.scene.control.Label errorLabel;
    @FXML
    private TableView<Account> accounTableView;
    @FXML
    private TableColumn<Account, Number> numCol;
    @FXML
    private TableColumn<Account, String> accountNameCol, amountCol;
    @FXML
    private TableColumn<Account, Void> actionCol;
    @FXML
    private Pagination pagination;

    @FXML
    private void initialize() {
        user = SessionManager.getInstance().getLoggedInUser();

        // STT
        numCol.setCellValueFactory(column -> {
            Account acc = column.getValue();
            if (acc != null) {
                int idx = accounTableView.getItems().indexOf(acc);
                return new SimpleIntegerProperty(idx + 1);
            }
            return null;
        });

        accountNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        loadAccountTable();

        addAccount.setOnAction(e -> handleAddAccount());

        // === Thêm listener cho tìm kiếm ===
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterAccountList(newValue);
        });
    }

    /** Nạp dữ liệu tài khoản vào bảng */
    private void loadAccountTable() {
        try {
            accountList.clear();
            accountList.addAll(accountService.getAccountsByUserId(user.getId()));

            int pageCount = (int) Math.ceil((double) accountList.size() / ROWS_PER_PAGE);
            pagination.setPageCount(pageCount == 0 ? 1 : pageCount);

            accounTableView.setItems(accountList);

            addButtonToTable();
        } catch (Exception e) {
            System.err.println("Error when loadAccountTable: " + e.getMessage());
        }
    }

    /** Thêm nút thao tác: Chi tiết & Chỉnh sửa (chỉ sửa tên) */
    private void addButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {

            private final Button detailBtn = new Button("Chi tiết");
            private final Button editBtn = new Button("Chỉnh sửa");
            private final HBox actionBox = new HBox(8, detailBtn, editBtn);

            {
                detailBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");

                // === Nút "Chi tiết" ===
                detailBtn.setOnAction(event -> {
                    Account account = getTableView().getItems().get(getIndex());
                    String info = "Tên tài khoản: " + account.getName()
                            + "\nSố dư (hệ thống): " + account.getBalance()
                            + "\nNgày tạo: " + account.getCreatedAt();

                    showAlert("Chi tiết tài khoản", info, Alert.AlertType.INFORMATION);
                });

                // === Nút "Chỉnh sửa" (chỉ cho phép sửa tên) ===
                editBtn.setOnAction(event -> {
                    Account account = getTableView().getItems().get(getIndex());

                    TextInputDialog dialog = new TextInputDialog(account.getName());
                    dialog.setTitle("Chỉnh sửa tài khoản");
                    dialog.setHeaderText(null);
                    dialog.setContentText("Nhập tên mới cho tài khoản:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(newName -> {
                        newName = newName.trim();
                        if (newName.isEmpty()) {
                            showAlert("Lỗi", "Tên tài khoản không được để trống!", Alert.AlertType.WARNING);
                            return;
                        }

                        try {
                            account.setName(newName);

                            // Gọi service cập nhật tên tài khoản
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
                            ex.printStackTrace();
                            showAlert("Lỗi", "Không thể cập nhật: " + ex.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });
    }

   /** Thêm tài khoản mới */
private void handleAddAccount() {
    String name = accountTextField.getText().trim();
    Double amount = 0.0;

    if (name.isEmpty()) {
        errorLabel.setText("Tên tài khoản không được bỏ trống!");
        return;
    }

    // Kiểm tra trùng tên
    boolean exists = accountList.stream()
            .anyMatch(acc -> normalize(acc.getName()).equalsIgnoreCase(normalize(name)));

    if (exists) {
        errorLabel.setText("Tài khoản bị trùng, vui lòng tạo tên khác!");
        return;
    }

    try {
        accountService.addAccount(new Account(user.getId(), name, amount, LocalDateTime.now(), false));
        accountTextField.clear();
        errorLabel.setText("");
        loadAccountTable(); // reload bảng
    } catch (Exception e) {
        System.err.println("Error when addAccount: " + e.getMessage());
        errorLabel.setText("Thêm tài khoản thất bại: " + e.getMessage());
    }
}


    /** === Lọc danh sách tài khoản theo tên hoặc số dư === */
    private void filterAccountList(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            accounTableView.setItems(accountList);
            addHighlightToColumn(null);
            return;
        }

        String lowerKeyword = normalize(keyword.toLowerCase());

        ObservableList<Account> filtered = FXCollections.observableArrayList();
        for (Account acc : accountList) {
            String normalizedName = normalize(acc.getName().toLowerCase());
            String balanceStr = String.valueOf(acc.getBalance());

            if (normalizedName.contains(lowerKeyword) || balanceStr.contains(lowerKeyword)) {
                filtered.add(acc);
            }
        }

        accounTableView.setItems(filtered);
        addHighlightToColumn(lowerKeyword);
    }

    /** === Highlight phần khớp trong tên tài khoản === */
    private void addHighlightToColumn(String keyword) {
        accountNameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (keyword != null && !keyword.isEmpty()) {
                    String normalizedItem = normalize(item.toLowerCase());
                    int start = normalizedItem.indexOf(keyword);

                    if (start >= 0) {
                        int end = Math.min(item.length(), start + keyword.length());
                        Text before = new Text(item.substring(0, start));
                        Text match = new Text(item.substring(start, end));
                        Text after = new Text(item.substring(end));
                        match.setStyle("-fx-fill: black; -fx-font-weight: bold; -fx-background-color: yellow;");
                        TextFlow flow = new TextFlow(before, match, after);
                        setGraphic(flow);
                        setText(null);
                    } else {
                        setText(item);
                        setGraphic(null);
                    }
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
        });
    }

    /** === Chuẩn hóa chuỗi để bỏ dấu tiếng Việt === */
    private String normalize(String input) {
        if (input == null) return "";
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{M}");
        return pattern.matcher(temp).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    /** Hiển thị thông báo */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
