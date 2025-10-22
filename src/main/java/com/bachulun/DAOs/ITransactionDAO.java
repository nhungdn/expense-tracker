package com.bachulun.DAOs;

import java.util.List;
import java.util.Map;

import com.bachulun.Models.Transaction;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

public interface ITransactionDAO {
    void addTransaction(Transaction transaction) throws InvalidInputException, DatabaseException;

    void updateTransaction(Transaction transaction) throws InvalidInputException, DatabaseException;

    void deleteTransaction(int transactionId) throws DatabaseException;

    List<Transaction> getTransactionByUser(int userId) throws DatabaseException;

    Map<String, Double> getMonthlyTotalsByTypeAndYear(int userId, String type, int year) throws DatabaseException;

}
