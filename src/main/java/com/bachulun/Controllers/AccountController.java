package com.bachulun.Controllers;

import java.time.LocalDateTime;

import com.bachulun.Models.Account;
import com.bachulun.Models.User;
import com.bachulun.Service.AccountService;
import com.bachulun.Service.CategoryService;
import com.bachulun.Service.IAccountService;
import com.bachulun.Service.ICategoryService;
import com.bachulun.Utils.SessionManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AccountController {

    private User user;
    private ObservableList<Account> accountList = FXCollections.observableArrayList();
    private final IAccountService accountService = new AccountService();
    private static final int ROWS_PER_PAGE = 10;

    @FXML
    private Button addAccount;
    @FXML
    private TextField searchField, accountTextField, amountTextField;
    @FXML
    Label errorLabel;
    @FXML
    private TableView<Account> accounTableView;
    @FXML
    private TableColumn<Account, Number> numCol;
    @FXML
    private TableColumn<Account, String> accountNameCol;
    @FXML
    private TableColumn<Account, Void> actionCol;
    @FXML
    private Pagination pagination;

    @FXML
    private void initialize() {
        user = SessionManager.getInstance().getLoggedInUser();

        numCol.setCellValueFactory(column -> {
            Account acc = column.getValue();
            if (acc != null) {
                int idx = accounTableView.getItems().indexOf(acc);
                return new SimpleIntegerProperty(idx + 1);
            }
            return null;
        });

        accountNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        loadAccountTable();

        addAccount.setOnAction(e -> handleAddAccount());
    }

    private void loadAccountTable() {
        try {
            accountList.clear();
            accountList.addAll(accountService.getAccountsByUserId(user.getId()));

            int pageCount = (int) Math.ceil((double) accountList.size() / ROWS_PER_PAGE);
            pagination.setPageCount(pageCount == 0 ? 1 : pageCount);

            accounTableView.setItems(accountList);

            // pagination.setPageFactory(pageIndex -> {
            // int fromIndex = pageIndex * ROWS_PER_PAGE;
            // int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, accountList.size());
            // System.out.println(fromIndex + " to " + toIndex);
            // accounTableView.setItems(FXCollections.observableArrayList(accountList.subList(fromIndex,
            // toIndex)));

            // return accounTableView;
            // });

            addButtonToTable();
        } catch (Exception e) {
            System.err.println("Error when loadAccountTable: " + e.getMessage());
        }
    }

    private void addButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {

            private final Button editBtn = new Button("Chi tiết");
            private final HBox actionBox = new HBox(5, editBtn);
            {
                editBtn.setStyle("-fx-background-color: #4c63afff; -fx-text-fill: white; -fx-font-size: 12px;");
            }
        });
    }

    private void handleAddAccount() {
        String name = accountTextField.getText();
        String amountText = amountTextField.getText();
        Double amount = 0.0;

        if (!amountText.trim().equals("")) {
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                errorLabel.setText("Số tiền không hợp lệ!");
                return;
            }
        } else {
            errorLabel.setText("Số tiền không được trống!");
            return;
        }

        errorLabel.setText("");
        try {
            accountService.addAccount(new Account(user.getId(), name, amount, LocalDateTime.now(), false));
            accountTextField.clear();
            amountTextField.clear();
            loadAccountTable();
        } catch (Exception e) {
            System.err.println("Error when addAccount: " + e.getMessage());
        }
    }

}
