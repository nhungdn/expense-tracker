package com.bachulun.DAOs;

import com.bachulun.Models.User;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

public interface IUserDAO {
    void registerUser(User user) throws InvalidInputException, DatabaseException;

    User loginUser(String username, String password) throws InvalidInputException, DatabaseException;

    User getUserById(int id) throws DatabaseException;

    public void updateUser(User user) throws InvalidInputException, DatabaseException;
}
