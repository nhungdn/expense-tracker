package com.bachulun.Service;

import com.bachulun.Models.User;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

// Service la cau noi giua controllers (UI logic) va DAOs (database access)
// Giup controllers tap trung vao UI va DAOs tap trung vao lam viec voi database
//=> de bao tri hon

public interface IUserService {
    void registerUser(User user) throws InvalidInputException, DatabaseException;

    User loginUser(String username, String password) throws InvalidInputException, DatabaseException;

    User getUserById(int id) throws DatabaseException;

    void updateUserInfor(User user) throws InvalidInputException, DatabaseException;

    void updateUserPassword(User user, String currentPassword, String newPassword)
            throws InvalidInputException, DatabaseException;
}
