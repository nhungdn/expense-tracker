package com.bachulun.Controllers;

import com.bachulun.Models.User;
import com.bachulun.Service.IUserService;
import com.bachulun.Service.UserService;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.SessionManager;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.control.Label;

public class SettingsController {
    private User currentUser;
    private MenuController menuController;
    IUserService userService = new UserService();

    // Dieu huong
    @FXML
    Button userInforButton;
    @FXML
    Button changePasswordButton;
    @FXML
    AnchorPane userInforPane;
    @FXML
    AnchorPane changePasswordPane;

    // Thong tin ca nhan
    @FXML
    Label fullNameLabel;
    @FXML
    TextField firstNameTf;
    @FXML
    TextField lastNameTf;
    @FXML
    TextField emailTf;
    @FXML
    TextField userNameTf;
    @FXML
    Button confirmUpdateUserInforButton;
    @FXML
    Label errorLabel;

    // Doi mat khau
    @FXML
    PasswordField currentPasswordField;
    @FXML
    PasswordField newPasswordField;
    @FXML
    PasswordField confirmNewPasswordField;
    @FXML
    Label errorPasswordLabel;
    @FXML
    Button confirmChangePasswordButton;

    @FXML
    private void initialize() {
        changePasswordPane.setVisible(false);
        userInforPane.setVisible(false);

        errorLabel.setText("");
        errorPasswordLabel.setText("");

        // navigate
        userInforButton.setOnAction(e -> userInforPane());
        changePasswordButton.setOnAction(e -> changePasswordPane());
        //Hiện bảng thông tín cá nhân ngay khi ấn vô mục cá nhân
        userInforPane();
    }

    private void userInforPane() {
        errorLabel.setText("");
        setActiveButton(userInforButton);

        changePasswordPane.setVisible(false);
        userInforPane.setVisible(true);

        currentUser = SessionManager.getInstance().getLoggedInUser();

        fullNameLabel.setText(currentUser.getLastName() + " " + currentUser.getFirstName());
        firstNameTf.setText(currentUser.getFirstName());
        lastNameTf.setText(currentUser.getLastName());
        emailTf.setText(currentUser.getEmail());
        userNameTf.setText(currentUser.getUsername());

        confirmUpdateUserInforButton.setOnAction(e -> {
            String firstName = firstNameTf.getText().trim();
            String lastName = lastNameTf.getText().trim();
            String email = emailTf.getText().trim();
            String username = userNameTf.getText().trim();

            try {
                userService.updateUserInfor(new User(currentUser.getId(), firstName, lastName, username,
                        currentUser.getPassword(), email, currentUser.getCreatedAt()));

                SessionManager.getInstance()
                        .setLoggedInUser(new User(currentUser.getId(), firstName, lastName, username,
                                currentUser.getPassword(), email, currentUser.getCreatedAt()));

                fullNameLabel.setText(lastName + " " + firstName);
                menuController.setWelcomeLabel(firstName);

                // Thong bao thanh cong va tu dong bien mat sau 5s
                errorLabel.setText("Cập nhật thành công");
                errorLabel.setTextFill(Color.GREEN);
                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(event -> errorLabel.setText(""));
                delay.play();
            } catch (InvalidInputException ex) {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText(ex.getMessage());
            } catch (DatabaseException ex) {
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Lỗi hệ thống. Vui lòng thử lại sau!");
            }
        });
    }

    private void changePasswordPane() {
        errorPasswordLabel.setText("");
        setActiveButton(changePasswordButton);

        userInforPane.setVisible(false);
        changePasswordPane.setVisible(true);

        confirmChangePasswordButton.setOnAction(e -> {
            currentUser = SessionManager.getInstance().getLoggedInUser();

            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmNewPassword = confirmNewPasswordField.getText();

            if (!newPassword.equals(confirmNewPassword)) {
                errorPasswordLabel.setTextFill(Color.RED);
                errorPasswordLabel.setText("Mật khẩu mới không khớp nhau!");
                return;
            }

            try {
                userService.updateUserPassword(currentUser, currentPassword, newPassword);
                int[] seconds = { 5 };
                errorPasswordLabel.setTextFill(Color.GREEN);
                errorPasswordLabel
                        .setText("Đổi mật khẩu thành công. Bạn sẽ tự động đăng xuất sau " + seconds[0] + " giây");
                Timeline[] timeline = new Timeline[1];
                timeline[0] = new Timeline(
                        new KeyFrame(Duration.seconds(1), event -> {
                            seconds[0]--;
                            if (seconds[0] > 0) {
                                errorPasswordLabel.setText("Đổi mật khẩu thành công. Bạn sẽ tự động đăng xuất sau "
                                        + seconds[0] + " giây");
                            } else {
                                // Khi đếm về 0
                                timeline[0].stop();
                                menuController.handleLogout();
                            }
                        }));
                timeline[0].setCycleCount(seconds[0]); // chạy 5 lần
                timeline[0].play();
            } catch (InvalidInputException ex) {
                errorPasswordLabel.setTextFill(Color.RED);
                errorPasswordLabel.setText(ex.getMessage());
            } catch (DatabaseException ex) {
                errorPasswordLabel.setTextFill(Color.RED);
                errorPasswordLabel.setText("Lỗi hệ thống. Vui lòng thử lại sau!");
            }
        });
    }

    private void setActiveButton(Button activeBtn) {
        userInforButton.getStyleClass().remove("active");
        changePasswordButton.getStyleClass().remove("active");

        if (!activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }
    }

    public void setMenuController(MenuController controller) {
        this.menuController = controller;
    }
}
