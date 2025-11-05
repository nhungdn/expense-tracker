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
import com.bachulun.Utils.DatabaseConnection;
import com.bachulun.Utils.DatabaseException;

public class TransactionDAO implements ITransactionDAO {

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
            pstmt.setInt(6, transaction.getId()); // ✅ Sửa lại ở đây
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error when updateTransaction: " + e.getMessage());
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

    @Override
    public Transaction getTransactionById(int transactionId) throws DatabaseException {
        String sql = "SELECT * FROM TransactionTable WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            return new Transaction(
                    transactionId,
                    rs.getInt("account_id"),
                    rs.getInt("category_id"),
                    rs.getDouble("amount"),
                    rs.getString("type"),
                    rs.getString("description"),
                    rs.getTimestamp("transaction_date").toLocalDateTime(),
                    rs.getTimestamp("created_at").toLocalDateTime());
        } catch (SQLException e) {
            System.err.println("Error when getTransactionById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Transaction> getTransactionByUserId(int userId) throws DatabaseException {
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

    @Override
    public List<Transaction> getTransactionByAccountId(int accountId) throws DatabaseException {
        String sql = "SELECT * FROM TransactionTable WHERE account_id = ?";

        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, accountId);
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
                        rs.getTimestamp("created_at").toLocalDateTime()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Transaction> getTransactionByCategoryId(int categoryId) throws DatabaseException {
        String sql = "SELECT * FROM TransactionTable WHERE category_id = ?";

        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, categoryId);
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
                        rs.getTimestamp("created_at").toLocalDateTime()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Transaction> getLatestTransactions(int userId, int limit) throws DatabaseException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
                    SELECT t.*
                    FROM TransactionTable t
                    JOIN Accounts a ON t.account_id = a.id
                    WHERE a.user_id = ?
                    ORDER BY t.transaction_date DESC
                    LIMIT ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        rs.getInt("category_id"),
                        rs.getDouble("amount"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getTimestamp("transaction_date").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime());
                transactions.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error when getLatestTransactions: " + e.getMessage());
            e.printStackTrace();
        }

        return transactions;
    }

    @Override
    public Map<Integer, Double> getCategoryTotalsForMonth(int month, String type, int year) throws DatabaseException {
        Map<Integer, Double> totals = new LinkedHashMap<>();
        String sql = """
                SELECT
                    category_id,
                    SUM(amount) as total
                FROM TransactionTable
                WHERE strftime('%m', datetime(transaction_date / 1000, 'unixepoch', '+7 hours')) = ? AND strftime('%Y', datetime(transaction_date / 1000, 'unixepoch', '+7 hours')) = ? AND type = ?
                GROUP BY category_id;
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, String.format("%02d", month));
            pstmt.setString(2, String.valueOf(year));
            pstmt.setString(3, type);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                totals.put(rs.getInt("category_id"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error when getCategoryTotalsForMonth: " + e);
        }

        return totals;
    }

    @Override
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