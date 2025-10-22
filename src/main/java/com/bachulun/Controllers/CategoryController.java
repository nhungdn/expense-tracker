package com.bachulun.Controllers;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bachulun.DAOs.CategoryDAO;
import com.bachulun.Models.Category;
import com.bachulun.Models.User;
import com.bachulun.Service.CategoryService;
import com.bachulun.Service.ICategoryService;
import com.bachulun.Utils.SessionManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class CategoryController {

    private User user;
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ICategoryService cateService = new CategoryService();

    @FXML
    private Button addCategory;
    @FXML
    private TextField searchField, categoryTextField;
    @FXML
    Label errorLabel;
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, Number> numCol;
    @FXML
    private TableColumn<Category, String> categoryNameCol;
    @FXML
    private TableColumn<Category, Void> actionCol;
    @FXML
    private Pagination pagination;

    @FXML
    private void initialize() {
        user = SessionManager.getInstance().getLoggedInUser();

        numCol.setCellValueFactory(column -> {
            Category cat = column.getValue();
            if (cat != null) {
                int idx = categoryTable.getItems().indexOf(cat);
                return new SimpleIntegerProperty(idx + 1);
            }
            return null;
        });

        categoryNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        loadCategoryTable();

        addCategory.setOnAction(e -> handleAddCategory());
    }

    private void loadCategoryTable() {
        try {
            categoryList.clear();
            categoryList.addAll(cateService.getCategoryByUserId(user.getId()));
            categoryTable.setItems(categoryList);

            addButtonToTable();
        } catch (Exception e) {
            System.err.println("Error when loadCategoryTable: " + e.getMessage());
        }
    }

    private void addButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {

            private final Button editBtn = new Button("Chi tiết");
            private final HBox actionBox = new HBox(5, editBtn);
            {
                editBtn.setStyle("-fx-background-color: #4c63afff; -fx-text-fill: white; -fx-font-size: 12px;");

                // editBtn.setOnAction(event -> {
                // Category category = getTableView().getItems().get(getIndex());
                // TextInputDialog dialog = new TextInputDialog(category.getName());
                // dialog.setTitle("Chỉnh sửa danh mục");
                // dialog.setHeaderText(null);
                // dialog.setContentText("Nhập tên danh mục mới:");

                // dialog.showAndWait().ifPresent(newName -> {
                // if (!newName.trim().isEmpty()) {
                // cateService.updateCategory(category.getId(), newName.trim());
                // } else {
                // CategoryController.this.showAlert("Tên danh mục không được để trống!",
                // Alert.AlertType.WARNING);
                // }
                // });
            }
        });

        // }

        // @Override
        // protected void updateItem(Void item, boolean empty) {
        // super.updateItem(item, empty);
        // if (empty) {
        // setGraphic(null);
        // } else {
        // setGraphic(actionBox);
        // }
        // }
        // });
    }

    // private void showAlert(String message, Alert.AlertType type) {
    // Alert alert = new Alert(type);
    // alert.setHeaderText(null);
    // alert.setContentText(message);
    // alert.showAndWait();
    // }

    private void handleAddCategory() {
        String name = categoryTextField.getText();

        if (name.trim().equals("")) {
            errorLabel.setText("Danh mục không được bỏ trống!");
            return;
        }
        errorLabel.setText("");
        CategoryDAO cateDao = new CategoryDAO();
        try {
            cateDao.addCategory(new Category(user.getId(), name, LocalDateTime.now(), false));
            categoryTextField.clear();
            loadCategoryTable();
        } catch (Exception e) {
            System.err.println("Error when addCategory: " + e.getMessage());
        }
    }

}
