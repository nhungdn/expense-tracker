package com.bachulun.Controllers;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.bachulun.Models.Transaction;
import com.bachulun.Models.User;
import com.bachulun.Service.*;
import com.bachulun.Utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Pair;

public class TransactionController {

@FXML
private TableView<EditHistory> historyTable;
@FXML
private TableColumn<EditHistory, String> timeCol, oldDescCol, newDescCol;
@FXML
private TableColumn<EditHistory, Number> transactionIdCol, oldAmountCol, newAmountCol;

private final ObservableList<EditHistory> editHistoryList = FXCollections.observableArrayList();


    private User currentUser;
    private Map<String, Integer> accountMap = new LinkedHashMap<>();
    private Map<String, Integer> categoryMap = new LinkedHashMap<>();
    private final ObservableList<Transaction> masterList = FXCollections.observableArrayList();
    private final ObservableList<String> editLogList = FXCollections.observableArrayList(); // ✅ Lưu lịch sử chỉnh sửa
    private FilteredList<Transaction> filteredList;
    private final ITransactionService tranService = new TransactionService();
    private final IAccountService accountService = new AccountService();
    private final ICategoryService cateService = new CategoryService();
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, String> dateCol, typeCol, categoryCol, amountCol, accountCol, descriptionCol;
    @FXML
    private TableColumn<Transaction, Void> actionCol;
    @FXML
    private DatePicker fromDatePicker, toDatePicker;
    @FXML
    private ComboBox<String> typeComboBox, categoryComboBox, accountComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private Button addTransaction;
    @FXML
    private Pagination pagination;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getLoggedInUser();

        filteredList = new FilteredList<>(masterList, p -> true);
        transactionTable.setItems(filteredList);

        addTransaction.setOnAction(e -> handleAddTransaction());
        accountComboBox.setOnAction(e -> updateFilter());
        typeComboBox.setOnAction(e -> updateFilter());
        categoryComboBox.setOnAction(e -> updateFilter());
        fromDatePicker.setOnAction(e -> updateFilter());
        toDatePicker.setOnAction(e -> updateFilter());

        try {
            accountMap = accountService.getAllAccountIdAndNameByUser(currentUser.getId());
            categoryMap = cateService.getAllCategoryIdAndNameByUserId(currentUser.getId());
        } catch (Exception e) {
            System.err.println(e);
        }

        loadDataFromDB();
        loadTableView();
        loadHistoryTable();
        loadAccountList();
        loadCategoryList();
        loadTypeList();
    }

private void loadTableView() {
    // Cập nhật số trang
    int pageCount = (int) Math.ceil((double) masterList.size() / ROWS_PER_PAGE);
    pagination.setPageCount(pageCount == 0 ? 1 : pageCount);

    // Gán dữ liệu cho từng cột
    dateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDateDisplay"));
    typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
    amountCol.setCellValueFactory(new PropertyValueFactory<>("amountDisplay"));
    accountCol.setCellValueFactory(new PropertyValueFactory<>("accountName"));
    descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

    // Chỉnh cột Thao tác
    actionCol.setText("Thao tác");
    actionCol.setPrefWidth(180);   // vừa đủ cho 2 button
    actionCol.setMinWidth(180);

    addActionButtonsToTable();

    // ✅ Bỏ thanh cuộn ngang bằng cách để TableView tự co các cột
    transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    // Đặt dữ liệu FilteredList
    transactionTable.setItems(filteredList);
}

    private void loadHistoryTable() {
    timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
    transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
    oldAmountCol.setCellValueFactory(new PropertyValueFactory<>("oldAmount"));
    newAmountCol.setCellValueFactory(new PropertyValueFactory<>("newAmount"));
    oldDescCol.setCellValueFactory(new PropertyValueFactory<>("oldDesc"));
    newDescCol.setCellValueFactory(new PropertyValueFactory<>("newDesc"));

    historyTable.setItems(editHistoryList);
}


/** Thêm 2 nút Chi tiết + Chỉnh sửa vào cột Thao tác */
private void addActionButtonsToTable() {
    actionCol.setCellFactory(param -> new TableCell<>() {
        private final Button detailBtn = new Button("Chi tiết");
        private final Button editBtn = new Button("Chỉnh sửa");
        private final HBox box = new HBox(8, detailBtn, editBtn);

        {
            // Style cho button
            detailBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
            editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");

            // Giữ button không bị cắt
            detailBtn.setMaxWidth(Double.MAX_VALUE);
            editBtn.setMaxWidth(Double.MAX_VALUE);

            box.setAlignment(Pos.CENTER);

            // Sự kiện Chi tiết
            detailBtn.setOnAction(e -> {
                Transaction t = getTableRow().getItem();
                if (t != null) {
                    String info = String.format(
                            "Ngày: %s\nLoại: %s\nDanh mục: %s\nSố tiền: %s\nTài khoản: %s\nMô tả: %s",
                            t.getTransactionDateDisplay(), t.getType(), t.getCategoryName(),
                            t.getAmountDisplay(), t.getAccountName(), t.getDescription());
                    showAlert("Chi tiết giao dịch", info, Alert.AlertType.INFORMATION);
                }
            });

            // Sự kiện Chỉnh sửa
            editBtn.setOnAction(e -> {
                Transaction t = getTableRow().getItem();
                if (t != null) showEditDialog(t);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(box);
            }
        }
    });
}


    /** ✅ Dialog chỉnh sửa số tiền + mô tả + ghi log */
    private void showEditDialog(Transaction t) {
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Chỉnh sửa giao dịch");
    dialog.setHeaderText(null);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    TextField amountField = new TextField(String.valueOf(t.getAmount()));
    TextField descField = new TextField(t.getDescription());

    grid.add(new Label("Số tiền:"), 0, 0);
    grid.add(amountField, 1, 0);
    grid.add(new Label("Mô tả:"), 0, 1);
    grid.add(descField, 1, 1);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(button -> {
        if (button == ButtonType.OK) {
            return new Pair<>(amountField.getText(), descField.getText());
        }
        return null;
    });

    Optional<Pair<String, String>> result = dialog.showAndWait();
    result.ifPresent(pair -> {
        String amountText = pair.getKey().trim();
        String newDesc = pair.getValue().trim();
        double newAmount;

        try {
            newAmount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            showAlert("Lỗi", "Số tiền phải là số hợp lệ!", Alert.AlertType.WARNING);
            return;
        }

        double oldAmount = t.getAmount();
        String oldDesc = t.getDescription();

        try {
            // ✅ Cập nhật trong DB
            t.setAmount(newAmount);
            t.setDescription(newDesc);
            tranService.updateTransaction(t);

            // ✅ Cập nhật lại trong danh sách đang hiển thị (không load lại toàn bảng)
            for (int i = 0; i < masterList.size(); i++) {
                if (masterList.get(i).getId() == t.getId()) {
                    masterList.set(i, t);
                    break;
                }
            }

            // ✅ Ghi log chỉnh sửa
            logEditAction(t, oldAmount, newAmount, oldDesc, newDesc);

            transactionTable.refresh();
            showAlert("Thành công", "Đã cập nhật giao dịch!", Alert.AlertType.INFORMATION);

        } catch (Exception ex) {
            showAlert("Lỗi", "Không thể cập nhật: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    });
}

    /** ✅ Hàm ghi log chỉnh sửa */
    private void logEditAction(Transaction t, double oldAmount, double newAmount, String oldDesc, String newDesc) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String logEntry = String.format("[%s] %s đã chỉnh sửa giao dịch #%d:\n  - Số tiền: %.2f → %.2f\n  - Mô tả: \"%s\" → \"%s\"\n",
                time, currentUser.getUsername(), t.getId(), oldAmount, newAmount, oldDesc, newDesc);

        editLogList.add(logEntry);

        // Ghi ra file
        try (FileWriter fw = new FileWriter("transaction_edit_log.txt", true)) {
            fw.write(logEntry + "\n");
        } catch (IOException e) {
            System.err.println("Không thể ghi file log: " + e.getMessage());
        }
        editHistoryList.add(new EditHistory(time, t.getId(), oldAmount, newAmount, oldDesc, newDesc));

    }

    private void loadAccountList() {
        List<String> accounts = accountMap.keySet().stream().collect(Collectors.toList());
        accounts.add(0, "Tất cả");
        accountComboBox.setItems(FXCollections.observableArrayList(accounts));
        accountComboBox.getSelectionModel().selectFirst();
    }

    private void loadCategoryList() {
        List<String> categories = categoryMap.keySet().stream().collect(Collectors.toList());
        categories.add(0, "Tất cả");
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.getSelectionModel().selectFirst();
    }

    private void loadTypeList() {
        typeComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Thu", "Chi"));
        typeComboBox.getSelectionModel().selectFirst();
    }

    private void loadDataFromDB() {
        masterList.clear();
        try {
            masterList.addAll(tranService.getTransactionByUserId(currentUser.getId()));
        } catch (Exception e) {
            System.err.println("Error when load data from DB: " + e.getMessage());
        }
    }

    private void updateFilter() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String type = typeComboBox.getValue();
        String category = categoryComboBox.getValue();

        Predicate<Transaction> predicate = t -> {
            if (from != null && t.getTransactionDate().toLocalDate().isBefore(from))
                return false;
            if (to != null && t.getTransactionDate().toLocalDate().isAfter(to))
                return false;

            if (type != null && !type.equals("Tất cả")) {
                if (t.getType() == null || !t.getType().equalsIgnoreCase(type))
                    return false;
            }

            if (category != null && !category.equals("Tất cả")) {
                if (t.getCategoryName() == null || !t.getCategoryName().equalsIgnoreCase(category))
                    return false;
            }

            return true;
        };
        filteredList.setPredicate(predicate);
    }

    private void handleAddTransaction() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AddTransaction.fxml"));
        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Thêm giao dịch");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error when load AddTransaction.fxml: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
