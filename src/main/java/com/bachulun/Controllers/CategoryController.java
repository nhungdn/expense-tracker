package com.bachulun.Controllers;

import java.time.LocalDateTime;

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
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;

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

        categoryNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));

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

    /**
     * Thêm 2 nút "Chi tiết" và "Chỉnh sửa" vào cột thao tác
     */
    private void addButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = new Button("Chi tiết");
            private final Button editBtn = new Button("Chỉnh sửa");
            private final HBox actionBox = new HBox(8, detailBtn, editBtn);

            {
                detailBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 12px;");
                editBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 12px;");

                // --- Nút Xem chi tiết ---
                detailBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    String detailMsg = String.format(
                            "📂 Tên danh mục: %s\n🕒 Ngày tạo: %s\n🔗 Mã danh mục: %d",
                            category.getName(),
                            category.getCreatedAt(),
                            category.getId()
                    );

                    showAlert(detailMsg, Alert.AlertType.INFORMATION);
                });

                // --- Nút Chỉnh sửa ---
                editBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    TextInputDialog dialog = new TextInputDialog(category.getName());
                    dialog.setTitle("Chỉnh sửa danh mục");
                    dialog.setHeaderText(null);
                    dialog.setContentText("Nhập tên danh mục mới:");

                    dialog.showAndWait().ifPresent(newName -> {
                        if (!newName.trim().isEmpty()) {
                            try {
                                cateService.updateCategory(category.getId(), newName.trim());
                                showAlert("Cập nhật danh mục thành công!", Alert.AlertType.INFORMATION);
                                loadCategoryTable();
                            } catch (Exception ex) {
                                showAlert("Lỗi khi cập nhật: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        } else {
                            showAlert("Tên danh mục không được để trống!", Alert.AlertType.WARNING);
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

    /**
     * Hiển thị thông báo tiện dụng
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
