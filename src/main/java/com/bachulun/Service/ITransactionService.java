package com.bachulun.Service;

import java.util.List;
import java.util.Map;

import com.bachulun.Models.Transaction;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

public interface ITransactionService {
    void addTransaction(Transaction transaction) throws InvalidInputException, DatabaseException;

    void updateTransaction(Transaction transaction) throws InvalidInputException, DatabaseException;

    void deleteTransaction(int transactionId) throws DatabaseException;

    List<Transaction> getTransactionByUserId(int userId) throws DatabaseException;

    List<Transaction> getTransactionByAccountId(int accountId) throws DatabaseException;

    Map<String, Double> getMonthlyTotalsByTypeAndYear(int userId, String type, int year) throws DatabaseException;
}