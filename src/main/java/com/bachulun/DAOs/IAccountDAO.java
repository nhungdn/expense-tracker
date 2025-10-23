package com.bachulun.DAOs;

import java.util.List;
import java.util.Map;

import com.bachulun.Models.Account;
import com.bachulun.Utils.DatabaseException;

public interface IAccountDAO {
    void addAccount(Account account) throws DatabaseException;

    void updateAccount(Account account) throws DatabaseException;

    void updateAccountBalance(int accountId, double balance) throws DatabaseException;

    void deleteAccount(int id) throws DatabaseException;

    Account getAccountById(int accountId) throws DatabaseException;

    List<Account> getAccountsByUserId(int userId) throws DatabaseException;

    Map<String, Integer> getAllAccountIdAndNameByUser(int userId) throws DatabaseException;
}
