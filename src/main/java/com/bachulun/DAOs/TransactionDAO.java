package com.bachulun.DAOs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import com.bachulun.Models.Transaction;
import com.bachulun.Service.AccountService;
import com.bachulun.Service.IAccountService;
import com.bachulun.Utils.DatabaseConnection;
import com.bachulun.Utils.DatabaseException;

public class TransactionDAO implements ITransactionDAO {
    private final IAccountService accountService = new AccountService();

    @Override
    public void addTransaction(Transaction transaction) throws DatabaseException {

        String sql = "INSERT INTO TransactionTable (account_id, category_id, amount, type, description, transaction_date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transaction.getAccountId());
            pstmt.setInt(2, transaction.getCategoryId());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getType());
            pstmt.setString(5, transaction.getDescription());
            pstmt.setTimestamp(6, Timestamp.valueOf(transaction.getTransactionDate()));
            pstmt.setTimestamp(7, Timestamp.valueOf(transaction.getCreatedAt()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error when addTransaction: " + e.getMessage());
        }

        // Cap nhat lai tien trong tai khoan
        accountService.updateAccountBalance(transaction.getAccountId(), transaction.getAmount(), transaction.getType());

    }

    @Override
    public void updateTransaction(Transaction transaction) throws DatabaseException {

        String sql = """
                UPDATE TransactionTable
                SET
                    account_id = ?,
                    category_id = ?,
                    amount = ?,
                    type = ?,
                    description = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transaction.getAccountId());
            pstmt.setInt(2, transaction.getCategoryId());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getType());
            pstmt.setString(5, transaction.getDescription());
            pstmt.setInt(6, transaction.getAccountId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error when updateCategory: " + e.getMessage());
        }
    }

    @Override
    public void deleteTransaction(int transactionId) throws DatabaseException {
        String sql = "DELETE FROM TransactionTable WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error when deleteTransaction: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactionByUser(int userId) throws DatabaseException {
        String sql = """
                    SELECT
                        t.id,
                        t.account_id,
                        t.category_id,
                        t.amount,
                        t.type,
                        t.description,
                        t.transaction_date,
                        t.created_at,
                        a.name AS accountName,
                        c.name AS categoryName
                    FROM TransactionTable t
                    JOIN Accounts a ON t.account_id = a.id
                    JOIN Categories c ON t.category_id = c.id
                    WHERE a.user_id = ?
                    ORDER BY t.transaction_date DESC
                """;

        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        rs.getInt("category_id"),
                        rs.getInt("amount"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getTimestamp("transaction_date").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("accountName"),
                        rs.getString("categoryName")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Double> getMonthlyTotalsByTypeAndYear(int userId, String type, int year)
            throws DatabaseException {
        String sql = """
                SELECT
                    strftime('%m', datetime(T.transaction_date / 1000, 'unixepoch', '+7 hours')) AS month,
                    SUM(T.amount) AS total
                FROM TransactionTable AS T
                JOIN Accounts AS A ON T.account_id = A.id
                WHERE
                    T.type = ?
                    AND strftime('%Y', datetime(T.transaction_date / 1000, 'unixepoch', '+7 hours')) = ?
                    AND A.user_id = ?
                GROUP BY month
                ORDER BY month;
                """;

        Map<String, Double> monthlyTotals = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setString(2, String.valueOf(year));
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String month = rs.getString("month");
                double total = rs.getDouble("total");
                monthlyTotals.put(month, total);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return monthlyTotals;
    }
}