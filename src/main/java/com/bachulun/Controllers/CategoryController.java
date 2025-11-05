package com.bachulun.Controllers;

import java.time.LocalDateTime;
import com.bachulun.DAOs.CategoryDAO;
import com.bachulun.Models.Category;
import com.bachulun.Models.User;
import com.bachulun.Service.CategoryService;
import com.bachulun.Service.ICategoryService;
import com.bachulun.Utils.SessionManager;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

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
    @FXML private TextField searchField;

    @FXML
    private void initialize() {
        user = SessionManager.getInstance().getLoggedInUser();

        numCol.setCellValueFactory(column -> {
            Category cat = column.getValue();
            return new SimpleIntegerProperty(categoryTable.getItems().indexOf(cat) + 1);
        });

        categoryNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));

        // ✅ Highlight tìm kiếm (fix lỗi mất chữ + hiệu ứng mượt)
        categoryNameCol.setCellFactory(col -> new TableCell<Category, String>() {
            private final TextFlow textFlow = new TextFlow();

            {
                textFlow.setLineSpacing(0);
                textFlow.setPadding(Insets.EMPTY);
                textFlow.setMaxWidth(330);
                textFlow.setPrefWidth(330);
                col.widthProperty().addListener((obs, oldW, newW) ->
                        textFlow.setPrefWidth(newW.doubleValue() - 10)
                );
            }

            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String keyword = searchField != null ? searchField.getText().toLowerCase() : "";
                    textFlow.getChildren().clear();

                    if (!keyword.isEmpty()) {
                        int startIndex = name.toLowerCase().indexOf(keyword);
                        if (startIndex >= 0) {
                            String before = name.substring(0, startIndex);
                            String match = name.substring(startIndex, startIndex + keyword.length());
                            String after = name.substring(startIndex + keyword.length());

                            Text beforeText = new Text(before);
                            Text matchText = new Text(match);
                            Text afterText = new Text(after);

                            // ✅ Chữ đen, nền vàng nhẹ hơn (đỡ trùng màu)
                            matchText.setFill(Color.BLACK);
                            matchText.setStyle("-fx-font-weight: bold;");
                            javafx.scene.shape.Rectangle highlight = new javafx.scene.shape.Rectangle();
                            highlight.setHeight(16);
                            highlight.setArcWidth(6);
                            highlight.setArcHeight(6);
                            highlight.setFill(Color.rgb(255, 230, 90, 0.7));
                            highlight.widthProperty().bind(matchText.layoutBoundsProperty().map(bounds -> bounds.getWidth() + 4));

                            // 👇 Hiệu ứng fade mượt
                            PauseTransition delay = new PauseTransition(Duration.millis(100));
                            delay.setOnFinished(ev -> {
                                FillTransition fade = new FillTransition(Duration.millis(400), highlight);
                                fade.setFromValue(Color.rgb(255, 230, 90, 0.7));
                                fade.setToValue(Color.rgb(255, 230, 90, 0.15));
                                fade.play();
                            });
                            delay.play();

                            javafx.scene.layout.StackPane highlightPane = new javafx.scene.layout.StackPane(highlight, matchText);
                            textFlow.getChildren().addAll(beforeText, highlightPane, afterText);
                        } else {
                            textFlow.getChildren().add(new Text(name));
                        }
                    } else {
                        textFlow.getChildren().add(new Text(name));
                    }

                    setGraphic(textFlow);
                    setPrefHeight(25);
                }
            }
        });

        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        actionCol.setPrefWidth(150); // vừa 2 nút

        addCategory.setOnAction(e -> handleAddCategory());

        // ✅ Tìm kiếm realtime
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch(newVal));
        }

        loadCategoryTable();
    }

    private void handleSearch(String keyword) {
        try {
            categoryList.clear();
            if (keyword == null || keyword.trim().isEmpty()) {
                categoryList.addAll(cateService.getCategoryByUserId(user.getId()));
            } else {
                String lowerKeyword = keyword.toLowerCase();
                for (Category c : cateService.getCategoryByUserId(user.getId())) {
                    if (c.getName().toLowerCase().contains(lowerKeyword)) {
                        categoryList.add(c);
                    }
                }
            }
            categoryTable.setItems(categoryList);
        } catch (Exception e) {
            System.err.println("Error in handleSearch: " + e.getMessage());
        }
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
                    String detailMsg = "📂 " + category.getName() +
                            "\n🕒 " + category.getCreatedAt() +
                            "\nID: " + category.getId();
                    showAlert(detailMsg, Alert.AlertType.INFORMATION);
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

    // ✅ Hàm thêm danh mục có kiểm tra trùng tên
    private void handleAddCategory() {
        String name = categoryTextField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Danh mục không được bỏ trống!");
            return;
        }

        try {
            boolean isDuplicate = cateService.getCategoryByUserId(user.getId()).stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(name));

            if (isDuplicate) {
                errorLabel.setText("Danh mục đã tồn tại, vui lòng chọn tên khác!");
                return;
            }

            errorLabel.setText("");
            new CategoryDAO().addCategory(new Category(user.getId(), name, LocalDateTime.now(), false));
            categoryTextField.clear();
            loadCategoryTable();
            showAlert("Thêm danh mục thành công!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            System.err.println("Error when addCategory: " + e.getMessage());
            errorLabel.setText("Đã xảy ra lỗi khi thêm danh mục!");
        }
    }
}
