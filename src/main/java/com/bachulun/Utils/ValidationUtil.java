package com.bachulun.Utils;

public class ValidationUtil {
    public static void validateFirstName(String firstName) throws InvalidInputException {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new InvalidInputException("Tên không được bỏ trống!");
        }
    }

    public static void validateLastName(String lastName) throws InvalidInputException {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new InvalidInputException("Họ không được bỏ trống!");
        }
    }

    public static void validateUsername(String username) throws InvalidInputException {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidInputException("Tên đăng nhập không được bỏ trống!");
        }
        if (username.length() > 50) {
            throw new InvalidInputException("Tên đăng nhập không dài quá 50 kí tự!");
        }
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new InvalidInputException("Tên đăng nhập không được chứa kí tự đặc biệt!");
        }
    }

    public static void validateEmail(String email) throws InvalidInputException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("Email không được bỏ trống!");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new InvalidInputException("Email không hợp lệ.");
        }
    }

    public static void validatePassword(String password) throws InvalidInputException {
        if (password == null || password.isEmpty()) {
            throw new InvalidInputException("Mật khẩu không được trống!");
        }
        if (password.length() < 6) {
            throw new InvalidInputException("Mật khẩu phải có ít nhất 6 kí tự!");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidInputException("Mật khẩu phải chứa ít nhất một kí tự in hoa!");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new InvalidInputException("Mật khẩu phải chứa ít nhất một kí tự in thường!");
        }
        if (!password.matches(".*\\d.*")) {
            throw new InvalidInputException("Mật khẩu phải chứa ít nhất một chữ số!");
        }
        if (password.length() > 30) {
            throw new InvalidInputException("Mật khẩu không dài quá 30 kí tự!");
        }
    }

    public static void validateAmount(double amount) throws InvalidInputException {
        if (amount < 0) {
            throw new InvalidInputException("Số tiền không được âm!");
        }
    }

    public static void validateAccount(String account) throws InvalidInputException {
        if (account != null && account.length() > 100) {
            throw new InvalidInputException("Tên tài khoản không dài quá 100 kí tự!");
        }
    }

    public static void validateCategory(String category) throws InvalidInputException {
        if (category != null && category.length() > 50) {
            throw new InvalidInputException("Tên danh mục không dài quá 50 kí tự!");
        }
    }

    public static void validateDescription(String description) throws InvalidInputException {
        if (description != null && description.length() > 1000) {
            throw new InvalidInputException("Mô tả không dài quá 1000 ki tự!");
        }
    }

}
