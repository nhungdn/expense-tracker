package com.bachulun.Service;

import java.util.List;
import java.util.Map;

import com.bachulun.DAOs.CategoryDAO;
import com.bachulun.DAOs.ICategoryDAO;
import com.bachulun.Models.Category;
import com.bachulun.Models.Transaction;
import com.bachulun.Models.User;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.SessionManager;
import com.bachulun.Utils.ValidationUtil;

public class CategoryService implements ICategoryService {
    private final ICategoryDAO cateDao = new CategoryDAO();
    private final ITransactionService transactionService = new TransactionService();

    @Override
    public void addCategory(Category category) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateCategory(category.getName());

        cateDao.addCategory(category);
    }

    @Override
    public void updateCategory(int categoryId, String newName) throws InvalidInputException, DatabaseException {
        ValidationUtil.validateCategory(newName);

        cateDao.updateCategory(categoryId, newName);
    }

    @Override
    public void deleteCategory(int categoryId) throws DatabaseException, InvalidInputException {
        User currentUser = SessionManager.getInstance().getLoggedInUser();
        // Xem danh muc co duoc xoa khong
        Category defaultCategory = cateDao.getDefaultCategoryByUserId(currentUser.getId());
        if (defaultCategory.getId() == categoryId) {
            throw new DatabaseException("Đây là danh mục mặc định bạn không thể xóa!");
        } else {
            // Chuyen cac giao dich ve danh muc mac dinh
            List<Transaction> tranList = transactionService.getTransactionByCategoryId(categoryId);
            for (Transaction t : tranList) {
                t.setCategoryId(defaultCategory.getId());
                transactionService.updateTransaction(t);
            }

            // Xoa
            cateDao.deleteCategory(categoryId);
        }
    }

    @Override
    public String getCategoryNameByCategoryId(int categoryId) throws DatabaseException {
        String categoryName = cateDao.getCategoryNameByCategoryId(categoryId);
        return categoryName;
    }

    @Override
    public List<Category> getCategoryByUserId(int userId) throws DatabaseException {
        List<Category> cateList = cateDao.getCategoryByUserId(userId);
        return cateList;
    }

    @Override
    public Map<String, Integer> getAllCategoryIdAndNameByUserId(int userId) throws DatabaseException {
        Map<String, Integer> cateList = cateDao.getAllCategoryIdAndNameByUserId(userId);
        return cateList;
    }

}
