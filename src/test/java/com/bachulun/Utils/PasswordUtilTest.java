package com.bachulun.Utils;

public class PasswordUtilTest {
    public static void main(String[] args) {
        String password = "mySecurePassword123";
        String hashed = PasswordUtil.hashPassword(password);
        System.out.println("Hashed: " + hashed);

        boolean valid = PasswordUtil.verifyPassword(password, hashed);
        System.out.println("Verify correct password: " + valid); // true

        boolean invalid = PasswordUtil.verifyPassword("wrongPassword", hashed);
        System.out.println("Verify wrong password: " + invalid); // false
    }
}
