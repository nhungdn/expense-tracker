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

    @FXML private Button addCategory;
    @FXML private TextField categoryTextField;
    @FXML Label errorLabel;
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Number> numCol;
    @FXML private TableColumn<Category, String> categoryNameCol;
    @FXML private TableColumn<Category, Void> actionCol;
    @FXML private Pagination pagination;

    @FXML
    private void initialize() {
        user = SessionManager.getInstance().getLoggedInUser();

        numCol.setCellValueFactory(column -> {
            Category cat = column.getValue();
            return new SimpleIntegerProperty(categoryTable.getItems().indexOf(cat) + 1);
        });

        categoryNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));

        loadCategoryTable();

        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        actionCol.setPrefWidth(150); // vừa 2 nút

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
                    Category category = getTableView().getItems().get(getIndex());
                    showAlert(
                            "📂 " + category.getName() +
                            "\n🕒 " + category.getCreatedAt()+
                            "\nID: " + category.getId(),
                            Alert.AlertType.INFORMATION
                    );
                });

                editBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    TextInputDialog dialog = new TextInputDialog(category.getName());
                    dialog.setTitle("Chỉnh sửa danh mục");
                    dialog.setContentText("Tên mới:");

                    dialog.showAndWait().ifPresent(newName -> {
                        if (!newName.trim().isEmpty()) {
                            try {
                                cateService.updateCategory(category.getId(), newName.trim());
                                showAlert("Cập nhật thành công!", Alert.AlertType.INFORMATION);
                                loadCategoryTable();
                            } catch (Exception ex) {
                                showAlert("Lỗi: " + ex.getMessage(), Alert.AlertType.ERROR);
                            }
                        } else {
                            showAlert("Không được để trống!", Alert.AlertType.WARNING);
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

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleAddCategory() {
        String name = categoryTextField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Danh mục không được bỏ trống!");
            return;
        }
        errorLabel.setText("");

        try {
            new CategoryDAO().addCategory(new Category(user.getId(), name, LocalDateTime.now(), false));
            categoryTextField.clear();
            loadCategoryTable();
        } catch (Exception e) {
            System.err.println("Error when addCategory: " + e.getMessage());
        }
    }
}
