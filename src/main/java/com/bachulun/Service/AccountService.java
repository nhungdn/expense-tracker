package com.bachulun.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.bachulun.DAOs.AccountDAO;
import com.bachulun.DAOs.IAccountDAO;
import com.bachulun.Models.Account;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.ValidationUtil;

public class AccountService implements IAccountService {

    private final IAccountDAO accountDAO = new AccountDAO();

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
    public void deleteAccount(int id) throws DatabaseException {
        accountDAO.deleteAccount(id);
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
