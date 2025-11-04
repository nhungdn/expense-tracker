package com.bachulun.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.bachulun.Models.User;
import com.bachulun.Utils.DatabaseConnection;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.PasswordUtil;

/*
    UserDAO tao User tu cac truy van o database va truyen sang controllers hoac SessionManager.
    UserDAO implements IUserDAO, la lop cung cap trien khai thuc tư cho giao dien IUserDAO.
 */

public class UserDAO implements IUserDAO {

    // Tao user moi
    @Override
    public void registerUser(User user) throws InvalidInputException, DatabaseException {
        String sql = "INSERT INTO Users (first_name, last_name, username, password, email, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stat = conn.prepareStatement(sql)) {
            stat.setString(1, user.getFirstName());
            stat.setString(2, user.getLastName());
            stat.setString(3, user.getUsername());
            stat.setString(4, user.getPassword());
            stat.setString(5, user.getEmail());
            stat.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
            stat.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                String message = e.getMessage();
                if (message.contains("username")) { // Trung username
                    throw new InvalidInputException("Username đã tồn tại!");
                } else if (message.contains("email")) { // Trung email
                    throw new InvalidInputException("Email đã tồn tại!");
                }
            }
            throw new DatabaseException("Failed to register user", e);
        }

    }

    // Dang nhap
    @Override
    public User loginUser(String username, String password) throws InvalidInputException, DatabaseException {

        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stat = conn.prepareStatement(sql)) {
            stat.setString(1, username);
            ResultSet rs = stat.executeQuery(); // Thuc thi cau lenh SQL

            if (rs.next()) { // Ton tai username dang muon log in
                String hashedPassword = rs.getString("password");
                if (PasswordUtil.verifyPassword(password, hashedPassword)) {
                    // mat khau dung thi tao doi tuong User
                    return new User(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("username"),
                            hashedPassword,
                            rs.getString("email"),
                            rs.getTimestamp("created_at").toLocalDateTime());
                } else {
                    throw new InvalidInputException("Mật khẩu không khớp!");
                }
            } else {
                throw new InvalidInputException("Username không tồn tại!");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to login user", e);

        }
    }

    @Override
    public User getUserById(int id) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at").toLocalDateTime());
            }
            throw new DatabaseException("User with ID " + id + " not found");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve user", e);
        }
    }

    @Override
    public void updateUserInfor(User user) throws InvalidInputException, DatabaseException {

        String sql = "UPDATE users SET first_name = ?, last_name = ?, username = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getUsername());
            stmt.setString(4, user.getEmail());
            stmt.setInt(5, user.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DatabaseException("No user found to update");
            }

        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                String message = e.getMessage();
                if (message.contains("username")) { // Trung username
                    throw new InvalidInputException("Username đã tồn tại!");
                } else if (message.contains("email")) { // Trung email
                    throw new InvalidInputException("Email đã tồn tại!");
                }
            }
            throw new DatabaseException("Failed to update user profile", e);
        }
    }

    @Override
    public void updateUserPassword(User user) throws InvalidInputException, DatabaseException {

        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getPassword());
            stmt.setInt(2, user.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new DatabaseException("No user found to update");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user password:", e);
        }
    }
}
