package com.bachulun.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.bachulun.Models.User;
import com.bachulun.Utils.DatabaseUtil;
import com.bachulun.Utils.PasswordUtil;

public class UserDAO {
    public void registerUser(User user) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
        String sql = "INSERT INTO Users (username, password, email, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement stat = conn.prepareStatement(sql)) {
            stat.setString(1, user.getUsername());
            stat.setString(2, hashedPassword);
            stat.setString(3, user.getEmail());
            stat.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));
            stat.executeUpdate();
        }
    }

    public User loginUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement stat = conn.prepareStatement(sql)) {
            stat.setString(1, username);
            try (ResultSet rs = stat.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (PasswordUtil.verifyPassword(password, hashedPassword)) {
                        // mat khau dung thi tao doi tuong User
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                hashedPassword,
                                rs.getString("email"),
                                rs.getTimestamp("created_at").toLocalDateTime());
                    }
                }
            }
        }
        return null;
    }
}
