package com.bachulun.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.bachulun.Models.Account;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

public interface IAccountService {
    void addAccount(Account account) throws InvalidInputException, DatabaseException;

    void updateAccount(Account account) throws InvalidInputException, DatabaseException;

    void updateAccountBalance(int accountId, double amount, String type) throws DatabaseException;

    void deleteAccount(int id) throws DatabaseException;

    Account getAccountById(int accountId) throws DatabaseException;

    Account getDefaultAccountByUserId(int userId) throws DatabaseException;

    List<Account> getAccountsByUserId(int userId) throws DatabaseException;

    Map<String, Integer> getAllAccountIdAndNameByUser(int userId) throws DatabaseException;
}
