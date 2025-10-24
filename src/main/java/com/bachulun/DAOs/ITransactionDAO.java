package com.bachulun.DAOs;

import java.util.List;
import java.util.Map;

import com.bachulun.Models.Transaction;
import com.bachulun.Utils.DatabaseException;

public interface ITransactionDAO {
    void addTransaction(Transaction transaction) throws DatabaseException;

    void updateTransaction(Transaction transaction) throws DatabaseException;

    void deleteTransaction(int transactionId) throws DatabaseException;

    Transaction getTransactionById(int transactionId) throws DatabaseException;

    List<Transaction> getTransactionByUserId(int userId) throws DatabaseException;

    List<Transaction> getTransactionByAccountId(int accountId) throws DatabaseException;

    List<Transaction> getTransactionByCategoryId(int categoryId) throws DatabaseException;

    List<Transaction> getLatestTransactions(int userId, int limit) throws DatabaseException;

    Map<Integer, Double> getCategoryTotalsForMonth(int month, String yype, int year) throws DatabaseException;

    Map<String, Double> getMonthlyTotalsByTypeAndYear(int userId, String type, int year) throws DatabaseException;

}