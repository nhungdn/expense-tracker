package com.bachulun.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.bachulun.DAOs.AccountDAO;
import com.bachulun.DAOs.IAccountDAO;
import com.bachulun.DAOs.ITransactionDAO;
import com.bachulun.DAOs.TransactionDAO;
import com.bachulun.Models.Account;
import com.bachulun.Models.Transaction;
import com.bachulun.Models.User;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.SessionManager;
import com.bachulun.Utils.ValidationUtil;

public class AccountService implements IAccountService {

    private final IAccountDAO accountDAO = new AccountDAO();
    private final ITransactionDAO transactionDAO = new TransactionDAO();

    @Override
    public void addAccount(Account account) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateAmount(account.getBalance());
        ValidationUtil.validateAccount(account.getName());

        accountDAO.addAccount(account);
    }

    @Override
    public void updateAccount(Account account) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateAmount(account.getBalance());
        ValidationUtil.validateAccount(account.getName());

        accountDAO.updateAccount(account);
    }

    @Override
    public void updateAccountBalance(int accountId, double amount, String type) throws DatabaseException {
        Account account = accountDAO.getAccountById(accountId);
        if (account != null) {
            Double money = 0.0;
            if (type.equals("Thu")) {
                money = account.getBalance() + amount;
            } else {
                money = account.getBalance() - amount;
            }
            accountDAO.updateAccountBalance(accountId, money);
        } else
            throw new DatabaseException("Tài khoản không tồn tại!");
    }

    @Override
    public void deleteAccount(int id) throws DatabaseException {
        // Xac dinh xem co phai tai khoan mac dinh khong
        Account deleteAccount = accountDAO.getAccountById(id);
        if (deleteAccount.getDeleteBan() == true)
            throw new DatabaseException("Đây là tài khoản mặc định. Bạn không thể xóa!");
        else {
            // Chuyen cac giao dich sang TK mac dinh
            User user = SessionManager.getInstance().getLoggedInUser();
            Account defaultAccount = accountDAO.getDefaultAccountByUserId(user.getId());
            List<Transaction> tranList = transactionDAO.getTransactionByAccountId(id);
            for (Transaction tran : tranList) {
                tran.setAccountId(defaultAccount.getId());

                transactionDAO.updateTransaction(tran);
            }
            // Xoa giao dich
            accountDAO.deleteAccount(id);
        }

    }

    @Override
    public Account getAccountById(int accountId) throws DatabaseException {
        return accountDAO.getAccountById(accountId);
    }

    @Override
    public Account getDefaultAccountByUserId(int userId) throws DatabaseException {
        Account account = accountDAO.getDefaultAccountByUserId(userId);
        return account;
    }

    @Override
    public List<Account> getAccountsByUserId(int userId) throws DatabaseException {
        List<Account> accList = accountDAO.getAccountsByUserId(userId);
        return accList;
    }

    @Override
    public Map<String, Integer> getAllAccountIdAndNameByUser(int userId) throws DatabaseException {
        Map<String, Integer> accList = accountDAO.getAllAccountIdAndNameByUser(userId);
        return accList;
    }
}
