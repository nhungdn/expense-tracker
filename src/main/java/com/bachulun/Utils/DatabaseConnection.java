package com.bachulun.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/Database/database.db";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Database connected.");
        } catch (SQLException e) {
            System.err.println("Database not connected: " + e.getMessage());
        }
        return connection;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Close connection successful.");
            } catch (SQLException e) {
                System.err.println("Error close connection: " + e.getMessage());
            }
        }
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stat = conn.createStatement();) {
            // Tao bang User
            String createUserTable = """
                    CREATE TABLE IF NOT EXISTS Users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        first_name TEXT NOT NULL,
                        last_name TEXT NOT NULL,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        created_at TIMESTAMP NOT NULL
                    )
                    """;
            stat.execute(createUserTable);

            // Tao bang Account
            String createAccountTable = """
                    CREATE TABLE IF NOT EXISTS Accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        balance REAL NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        delete_ban BOOLEAN NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
                    )
                    """;
            stat.execute(createAccountTable);

            // Tao bang Category
            String createCategoryTable = """
                    CREATE TABLE IF NOT EXISTS Categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        delete_ban BOOLEAN NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
                    )
                    """;
            stat.execute(createCategoryTable);

            // Tao bang Transaction
            String createTransactionTable = """
                    CREATE TABLE IF NOT EXISTS TransactionTable (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        account_id INTEGER NOT NULL,
                        category_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL CHECK(type IN ('Thu', 'Chi')),
                        description TEXT,
                        transaction_date TIMESTAMP NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        FOREIGN KEY (account_id) REFERENCES Accounts(id) ON DELETE CASCADE,
                        FOREIGN KEY (category_id) REFERENCES Categories(id) ON DELETE CASCADE
                    )
                    """;
            stat.execute(createTransactionTable);

            System.out.println("Database init successful.");

        } catch (SQLException e) {
            System.err.println("Error init Database: " + e.getMessage());
        }
    }
}
