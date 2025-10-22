package com.bachulun.Service;

import java.util.List;
import java.util.Map;

import com.bachulun.DAOs.ITransactionDAO;
import com.bachulun.DAOs.TransactionDAO;
import com.bachulun.Models.Transaction;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.ValidationUtil;

public class TransactionService implements ITransactionService {
    final private ITransactionDAO tranDao = new TransactionDAO();

    @Override
    public void addTransaction(Transaction transaction) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateAmount(transaction.getAmount());
        ValidationUtil.validateDescription(transaction.getDescription());

        tranDao.addTransaction(transaction);
    }

    @Override
    public void updateTransaction(Transaction transaction) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateAmount(transaction.getAmount());
        ValidationUtil.validateDescription(transaction.getDescription());

        tranDao.updateTransaction(transaction);
    }

    @Override
    public void deleteTransaction(int transactionId) throws DatabaseException {
        tranDao.deleteTransaction(transactionId);
    }

    @Override
    public List<Transaction> getTransactionByUser(int userId) throws DatabaseException {
        List<Transaction> tranList = tranDao.getTransactionByUser(userId);
        return tranList;
    }

    @Override
    public Map<String, Double> getMonthlyTotalsByTypeAndYear(int userId, String type, int year)
            throws DatabaseException {
        Map<String, Double> tranList = tranDao.getMonthlyTotalsByTypeAndYear(userId, type, year);
        return tranList;
    }

}
