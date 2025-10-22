package com.bachulun.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.bachulun.DAOs.IUserDAO;
import com.bachulun.DAOs.UserDAO;
import com.bachulun.Models.Account;
import com.bachulun.Models.Category;
import com.bachulun.Models.User;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.PasswordUtil;
import com.bachulun.Utils.SessionManager;
import com.bachulun.Utils.ValidationUtil;

public class UserService implements IUserService {

    private final IUserDAO userDAO = new UserDAO();
    private final IAccountService accountService = new AccountService();
    private final ICategoryService cateService = new CategoryService();

    @Override
    public void registerUser(User user) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateUsername(user.getUsername());
        ValidationUtil.validatePassword(user.getPassword());
        ValidationUtil.validateEmail(user.getEmail());

        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        userDAO.registerUser(user);
    }

    @Override
    public User loginUser(String username, String password) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateUsername(username);
        ValidationUtil.validatePassword(password);

        User user = userDAO.loginUser(username, password);
        SessionManager.getInstance().setLoggedInUser(user); // Dang nhap thanh cong thi luu vao SessionManager

        // Tao tai khoan va danh muc mac dinh cho lan dang nhap dau tien
        User currentUser = SessionManager.getInstance().getLoggedInUser();
        List<Account> accList = accountService.getAccountsByUserId(currentUser.getId());
        if (accList.isEmpty()) {
            accountService
                    .addAccount(new Account(currentUser.getId(), "Tài khoản mặc định", 0, LocalDateTime.now(), true));
            cateService.addCategory(new Category(currentUser.getId(), "Khác", LocalDateTime.now(), true));
        }
        return user;
    }

    @Override
    public User getUserById(int id) throws DatabaseException {
        User user = userDAO.getUserById(id);
        return user;
    }

    @Override
    public void updateUser(User user) throws InvalidInputException, DatabaseException {
        String username = user.getUsername().trim();
        String password = user.getPassword();
        String email = user.getEmail();

        ValidationUtil.validateUsername(username);
        ValidationUtil.validatePassword(password);
        ValidationUtil.validateEmail(email);

        String hashedPassword = PasswordUtil.hashPassword(password);
        user.setPassword(hashedPassword);

        try {
            userDAO.updateUser(user);
        } catch (DatabaseException e) {
            throw e;
        }
    }

}