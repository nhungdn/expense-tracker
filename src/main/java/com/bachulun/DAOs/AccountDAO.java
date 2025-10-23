package com.bachulun.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.bachulun.Models.Account;
import com.bachulun.Utils.DatabaseConnection;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.SessionManager;
import com.bachulun.Utils.ValidationUtil;

public class AccountDAO implements IAccountDAO {
    @Override
    public void addAccount(Account account) throws DatabaseException {
        String sql = "INSERT INTO Accounts (user_id, name, balance, created_at, delete_ban) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, account.getUserId());
            pstmt.setString(2, account.getName());
            pstmt.setDouble(3, account.getBalance());
            pstmt.setTimestamp(4, Timestamp.valueOf(account.getCreatedAt()));
            pstmt.setBoolean(5, account.getDeleteBan());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error when add Account", e);
        }
    }

    @Override
    public void updateAccount(Account account) throws DatabaseException {
        String sql = "UPDATE Accounts SET name = ?, balance = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getName());
            pstmt.setDouble(2, account.getBalance());
            pstmt.setInt(3, account.getId());
            pstmt.setInt(4, account.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error when update Account", e);
        }
    }

    @Override
    public void updateAccountBalance(int accountId, double balance) throws DatabaseException {
        String sql = "UPDATE Accounts SET balance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, balance);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error when update AccountBalance", e);
        }
    }

    @Override
    public void deleteAccount(int id) throws DatabaseException {
        int userId = SessionManager.getInstance().getLoggedInUser().getId();

        // Tim tai khoan mac dinh

        // Tai khoan dang xoa la tai khoan mac dinh -> khong duoc phep xoa

        // Khong phai -> chuyen transaction cua tai khoan dang xoa sang tai khoan mac
        // dinh
    }

    @Override
    public Account getAccountById(int accountId) throws DatabaseException {
        String sql = "SELECT * FROM Accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);

            ResultSet rs = pstmt.executeQuery();
            return new Account(
                    accountId,
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getDouble("balance"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getBoolean("delete_ban"));
        } catch (SQLException e) {
            System.err.println("Error when getAccountById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Account> getAccountsByUserId(int userId) throws DatabaseException {
        List<Account> accountList = new ArrayList<>();
        String sql = "SELECT * FROM Accounts WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account acc = new Account(
                            rs.getInt("id"),
                            userId,
                            rs.getString("name"),
                            rs.getDouble("balance"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getBoolean("delete_ban"));

                    accountList.add(acc);
                }
            }
            return accountList;
        } catch (SQLException e) {
            throw new DatabaseException("Error when get acount by userId", e);
        }

    }

    @Override
    public Map<String, Integer> getAllAccountIdAndNameByUser(int userId) throws DatabaseException {
        Map<String, Integer> accountList = new LinkedHashMap<>();
        String sql = "SELECT id, name FROM Accounts WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                accountList.put(rs.getString("name"), Integer.valueOf(rs.getInt("id")));
            }
            return accountList;
        } catch (SQLException e) {
            throw new DatabaseException("Error when get acount by userId", e);
        }

    }
}
