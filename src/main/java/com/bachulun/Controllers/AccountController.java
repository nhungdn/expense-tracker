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

        // === Thêm listener cho tìm kiếm ===
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterAccountList(newValue);
        });
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
<<<<<<< HEAD
                    String info = "Tên tài khoản: " + account.getName()
                            + "\nSố dư (hệ thống): " + account.getBalance()
                            + "\nNgày tạo: " + account.getCreatedAt();
=======
                    String info =
                        "Tên tài khoản: " + account.getName() +
                        "\nSố dư: " + account.getBalance() +
                        "\nNgày tạo: " + account.getCreatedAt();
>>>>>>> 63dbdc1f1d7962ac7f3b3f88c9b90eac88fd6c17

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

<<<<<<< HEAD
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
=======
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
>>>>>>> 63dbdc1f1d7962ac7f3b3f88c9b90eac88fd6c17
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
