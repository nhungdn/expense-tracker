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

    Map<String, Double> getMonthlyTotalsByTypeAndYear(int userId, String type, int year) throws DatabaseException;

}