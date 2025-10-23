package com.bachulun.Controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

import com.bachulun.Models.Transaction;
import com.bachulun.Models.User;
import com.bachulun.Service.AccountService;
import com.bachulun.Service.CategoryService;
import com.bachulun.Service.IAccountService;
import com.bachulun.Service.ICategoryService;
import com.bachulun.Service.ITransactionService;
import com.bachulun.Service.TransactionService;
import com.bachulun.Utils.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class TransactionController {

    private User currentUser;
    private Map<String, Integer> accountMap = new LinkedHashMap<>();
    private Map<String, Integer> categoryMap = new LinkedHashMap<>();
    private final ObservableList<Transaction> masterList = FXCollections.observableArrayList();
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
    private DatePicker fromDatePicker, toDatePicker;
    @FXML
    private TableColumn<Transaction, Void> actionCol;
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
        loadTableView(); // Cau hinh cac cot trong bang
        loadAccountList(); // Set up cac Account
        loadCategoryList(); // Set up cac Category
        loadTypeList(); // Set up cac Type
    }

    private void loadTableView() {
        int pageCount = (int) Math.ceil((double) masterList.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        // pagination.setPageFactory(this::createPage);

        dateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDateDisplay"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amountDisplay"));
        accountCol.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
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

    // private Node createPage(int pageIndex) {
    // int fromIndex = pageIndex * ROWS_PER_PAGE;
    // int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, masterList.size());
    // transactionTable.setItems(FXCollections.observableArrayList(masterList.subList(fromIndex,
    // toIndex)));
    // return new VBox(transactionTable);
    // }
}
