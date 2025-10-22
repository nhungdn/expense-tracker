package com.bachulun;

import com.bachulun.Utils.DatabaseConnection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
Cấu trúc thư mục
 * Controllers: Điều khiển quản lí giao diện (làm việc với các file fxml tương ứng) - xử lí sự kiện ng dùng(bấm nút, nhập...)
 *              Là cầu nối giữa giao diện người dùng (FXML) và logic xử lý (Models, Services)
 * 
 * DAOs (Data Access Object): Chứa các lớp truy cập dữ liệu trực tiếp từ Database
 * 
 * Models: Chứa các lớp mô hình, mỗi model tương ứng 1 bảng trong cơ sở dữ liệu
 * 
 * Service: Chứa các lớp xử lý nghiệp vụ (business project).
 *          La lớp trung gian giữa Controller và DAO
 * 
 * Utils: Chứa các lớp tiện ích, các hàm dùng chung cho nhiều nơi trong chương trình
 * 
 * Resources: Chứa các tài nguyên của chương trình (FXML, Database, Images, ...)
 * 
 * test: Dành cho Unit testing 
 */

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chua co DB thi bo note dong ben duoi
        // DatabaseConnection.initDatabase();

        Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Login.fxml"));
        primaryStage.setTitle("Expense Tracker");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}