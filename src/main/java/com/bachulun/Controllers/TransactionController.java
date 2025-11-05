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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
    private final ObservableList<String> editLogList = FXCollections.observableArrayList();
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

        // ✅ Tìm kiếm realtime + highlight
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilter();
            transactionTable.refresh(); // làm mới để highlight
        });
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

        // ✅ Gắn highlight renderer
        addHighlightToColumn(descriptionCol);
        addHighlightToColumn(categoryCol);
        addHighlightToColumn(accountCol);
        addHighlightToColumn(typeCol);
        addHighlightToColumn(amountCol);

        // Chỉnh cột Thao tác
        actionCol.setText("Thao tác");
        actionCol.setPrefWidth(180);
        actionCol.setMinWidth(180);

        addActionButtonsToTable();

        // ✅ Bỏ thanh cuộn ngang
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Đặt dữ liệu FilteredList
        transactionTable.setItems(filteredList);
    }

    // ✅ Hàm tạo cell highlight
    private void addHighlightToColumn(TableColumn<Transaction, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String keyword = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
                    if (!keyword.isEmpty() && item.toLowerCase().contains(keyword)) {
                        int start = item.toLowerCase().indexOf(keyword);
                        int end = start + keyword.length();

                        Text before = new Text(item.substring(0, start));
                        Text match = new Text(item.substring(start, end));
                        match.setFill(Color.GOLD);
                        match.setStyle("-fx-font-weight: bold;");
                        Text after = new Text(item.substring(end));

                        TextFlow flow = new TextFlow(before, match, after);
                        setGraphic(flow);
                        setText(null);
                    } else {
                        setText(item);
                        setGraphic(null);
                    }
                }
            }
        });
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

    private void addActionButtonsToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = new Button("Chi tiết");
            private final Button editBtn = new Button("Chỉnh sửa");
            private final HBox box = new HBox(8, detailBtn, editBtn);

            {
                detailBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");

                detailBtn.setMaxWidth(Double.MAX_VALUE);
                editBtn.setMaxWidth(Double.MAX_VALUE);
                box.setAlignment(Pos.CENTER);

                detailBtn.setOnAction(e -> {
                    Transaction t = getTableRow().getItem();
                    if (t != null) {
                        String info = String.format(
                            "Ngày: %s\nLoại: %s\nDanh mục: %s\nSố tiền: %s\nTài khoản: %s\nMô tả: %s",
                            t.getTransactionDateDisplay(), t.getType(), t.getCategoryName(),
                            t.getAmountDisplay(), t.getAccountName(), t.getDescription()
                        );
                        showAlert("Chi tiết giao dịch", info, Alert.AlertType.INFORMATION);
                    }
                });

                editBtn.setOnAction(e -> {
                    Transaction t = getTableRow().getItem();
                    if (t != null) showEditDialog(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
             }
        });
    }

    private void showEditDialog(Transaction t) {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Chỉnh sửa giao dịch");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField amountField = new TextField(String.valueOf(t.getAmount()));
        TextField descField = new TextField(t.getDescription());

        // Loại giao dịch
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("Thu", "Chi"));
        typeBox.getSelectionModel().select(t.getType());

        // Tài khoản
        ComboBox<String> accountBox = new ComboBox<>(FXCollections.observableArrayList(accountMap.keySet()));
        accountBox.getSelectionModel().select(t.getAccountName());

        // Danh mục
        ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(categoryMap.keySet()));
        categoryBox.getSelectionModel().select(t.getCategoryName());

        grid.add(new Label("Số tiền:"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Loại:"), 0, 2);
        grid.add(typeBox, 1, 2);
        grid.add(new Label("Tài khoản:"), 0, 3);
        grid.add(accountBox, 1, 3);
        grid.add(new Label("Danh mục:"), 0, 4);
        grid.add(categoryBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> button == ButtonType.OK ? t : null);

        Optional<Transaction> result = dialog.showAndWait();
        result.ifPresent(tran -> {
            double oldAmount = t.getAmount();
            String oldDesc = t.getDescription();
            String oldType = t.getType();
            String oldAccountName = t.getAccountName();
            String oldCategoryName = t.getCategoryName();

            try {
                double newAmount = Double.parseDouble(amountField.getText().trim());
                String newDesc = descField.getText().trim();
                String newType = typeBox.getValue();
                int newAccountId = accountMap.get(accountBox.getValue());
                int newCategoryId = categoryMap.get(categoryBox.getValue());

                // Cập nhật Transaction
                t.setAmount(newAmount);
                t.setDescription(newDesc);
                t.setType(newType);
                t.setAccountId(newAccountId);
                t.setAccountName(accountBox.getValue());
                t.setCategoryId(newCategoryId);
                t.setCategoryName(categoryBox.getValue());

                tranService.updateTransaction(t);

                for (int i = 0; i < masterList.size(); i++) {
                    if (masterList.get(i).getId() == t.getId()) {
                        masterList.set(i, t);
                        break;
                    }
                }

                logEditAction(t, oldAmount, newAmount, oldDesc, newDesc); // Thêm vào log
                transactionTable.refresh();
                showAlert("Thành công", "Đã cập nhật giao dịch!", Alert.AlertType.INFORMATION);

            } catch (NumberFormatException ex) {
                showAlert("Lỗi", "Số tiền phải là số hợp lệ!", Alert.AlertType.WARNING);
            } catch (Exception ex) {
                showAlert("Lỗi", "Không thể cập nhật: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void logEditAction(Transaction t, double oldAmount, double newAmount, String oldDesc, String newDesc) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String logEntry = String.format("[%s] %s đã chỉnh sửa giao dịch #%d:\n  - Số tiền: %.2f → %.2f\n  - Mô tả: \"%s\" → \"%s\"\n" +
                "  - Loại giao dịch: %s → %s\n  - Tài khoản: %s → %s\n  - Danh mục: %s → %s\n",
                time, currentUser.getUsername(), t.getId(), oldAmount, newAmount, oldDesc, newDesc,
                oldType, newType, oldAccountName, t.getAccountName(), oldCategoryName, t.getCategoryName());

        editLogList.add(logEntry);

        try (FileWriter fw = new FileWriter("transaction_edit_log.txt", true)) {
            fw.write(logEntry + "\n");
        } catch (IOException e) {
            System.err.println("Không thể ghi file log: " + e.getMessage());
        }

        editHistoryList.add(new EditHistory(time, t.getId(), oldAmount, newAmount, oldDesc, newDesc));
    }

    private void loadAccountList() {
        List<String> accounts = new ArrayList<>(accountMap.keySet());
        accounts.add(0, "Tất cả");
        accountComboBox.setItems(FXCollections.observableArrayList(accounts));
        accountComboBox.getSelectionModel().selectFirst();
    }

    private void loadCategoryList() {
        List<String> categories = new ArrayList<>(categoryMap.keySet());
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
        String keyword = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";

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

            if (!keyword.isEmpty()) {
                return (t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword))
                        || (t.getCategoryName() != null && t.getCategoryName().toLowerCase().contains(keyword))
                        || (t.getAccountName() != null && t.getAccountName().toLowerCase().contains(keyword))
                        || (t.getType() != null && t.getType().toLowerCase().contains(keyword))
                        || String.valueOf(t.getAmount()).contains(keyword);
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
