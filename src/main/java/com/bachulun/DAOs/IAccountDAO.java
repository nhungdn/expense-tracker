package com.bachulun.DAOs;

import java.util.List;
import java.util.Map;

import com.bachulun.Models.Account;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

public interface IAccountDAO {
    void addAccount(Account account) throws InvalidInputException, DatabaseException;

    void updateAccount(Account account) throws InvalidInputException, DatabaseException;

    void deleteAccount(int id) throws DatabaseException;

    List<Account> getAccountsByUserId(int userId) throws DatabaseException;

    Map<String, Integer> getAllAccountIdAndNameByUser(int userId) throws DatabaseException;
}
