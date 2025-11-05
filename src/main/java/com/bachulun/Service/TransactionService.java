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
    private ITransactionDAO tranDao = new TransactionDAO();
    private IAccountService accountService = new AccountService();

    @Override
    public void addTransaction(Transaction transaction) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateAmount(transaction.getAmount());
        ValidationUtil.validateDescription(transaction.getDescription());

        tranDao.addTransaction(transaction);

        // Cập nhật lại tiền trong tài khoản
        accountService.updateAccountBalance(transaction.getAccountId(), transaction.getAmount(), transaction.getType());
    }

    @Override
    public void updateTransaction(Transaction transaction) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateAmount(transaction.getAmount());
        ValidationUtil.validateDescription(transaction.getDescription());

        // Lấy thông tin giao dịch trước khi update
        Transaction oldTran = tranDao.getTransactionById(transaction.getId());

        // Rút giao dịch ra khỏi tài khoản để update mới
        if (oldTran.getType().equals("Thu"))
            accountService.updateAccountBalance(oldTran.getAccountId(), oldTran.getAmount(), "Chi");
        else
            accountService.updateAccountBalance(oldTran.getAccountId(), oldTran.getAmount(), "Thu");

        // Cập nhật lại giao dịch
        tranDao.updateTransaction(transaction);

        // Cập nhật lại tiền trong tài khoản
        accountService.updateAccountBalance(transaction.getAccountId(), transaction.getAmount(), transaction.getType());
    }

    @Override
    public void deleteTransaction(int transactionId) throws DatabaseException {
        tranDao.deleteTransaction(transactionId);
    }

    @Override
    public List<Transaction> getTransactionByUserId(int userId) throws DatabaseException {
        List<Transaction> tranList = tranDao.getTransactionByUserId(userId);
        return tranList;
    }

    @Override
    public List<Transaction> getLatestTransactions(int userId, int limit) throws DatabaseException {
        List<Transaction> tranList = tranDao.getLatestTransactions(userId, limit);
        return tranList;
    }

    @Override
    public List<Transaction> getTransactionByAccountId(int accountId) throws DatabaseException {
        List<Transaction> tranList = tranDao.getTransactionByAccountId(accountId);
        return tranList;
    }

    @Override
    public List<Transaction> getTransactionByCategoryId(int categoryId) throws DatabaseException {
        // Sửa lỗi ở đây: trước đó bạn gọi nhầm getTransactionByAccountId
        List<Transaction> tranList = tranDao.getTransactionByCategoryId(categoryId);
        return tranList;
    }

    @Override
    public Map<Integer, Double> getCategoryTotalsForMonth(int month, String type, int year) throws DatabaseException {
        Map<Integer, Double> tranList = tranDao.getCategoryTotalsForMonth(month, type, year);
        return tranList;
    }

    @Override
    public Map<String, Double> getMonthlyTotalsByTypeAndYear(int userId, String type, int year)
            throws DatabaseException {
        Map<String, Double> tranList = tranDao.getMonthlyTotalsByTypeAndYear(userId, type, year);
        return tranList;
    }
}
