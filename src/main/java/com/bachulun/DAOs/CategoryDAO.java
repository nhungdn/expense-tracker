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

import com.bachulun.Models.Category;
import com.bachulun.Utils.DatabaseConnection;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

public class CategoryDAO implements ICategoryDAO {
    @Override
    public void addCategory(Category category) throws InvalidInputException, DatabaseException {

        String sql = "INSERT INTO Categories (user_id, name, created_at, delete_ban) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, category.getUserId());
            pstmt.setString(2, category.getName());
            pstmt.setTimestamp(3, Timestamp.valueOf(category.getCreatedAt()));
            pstmt.setBoolean(4, category.getDeleteBan());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error when add Category: " + e.getMessage());
        }
    }

    @Override
    public void updateCategory(int categoryId, String newName) throws InvalidInputException, DatabaseException {

        String sql = "UPDATE Categories SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, categoryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error when updateCategory: " + e.getMessage());
        }
    }

    @Override
    public void deleteCategory(int categoryId) throws DatabaseException {
        String sql1 = "DELETE FROM Categories WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setInt(1, categoryId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error when deleteCategory: " + e.getMessage());
        }
    }

    @Override
    public List<Category> getCategoryByUserId(int userId) throws DatabaseException {
        String sql = """
                SELECT *
                FROM Categories
                WHERE user_id = ?
                """;

        List<Category> cateList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Category cate = new Category(rs.getInt("id"), userId, rs.getString("name"),
                        rs.getTimestamp("created_at").toLocalDateTime(), rs.getBoolean("delete_ban"));

                cateList.add(cate);
            }
        } catch (Exception e) {
            System.err.println("Error when getCategoryByUser: " + e.getMessage());
        }
        return cateList;
    }

    public Map<String, Integer> getAllCategoryIdAndNameByUserId(int userId) throws DatabaseException {
        String sql = "SELECT id, name FROM Categories WHERE user_id = ?";
        Map<String, Integer> categories = new LinkedHashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                categories.put(rs.getString("name"), Integer.valueOf(rs.getInt("id")));
            }
        } catch (SQLException e) {
            System.err.println("Error when ");
        }
        return categories;
    }
}
