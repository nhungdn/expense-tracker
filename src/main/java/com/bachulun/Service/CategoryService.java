package com.bachulun.Service;

import java.util.List;
import java.util.Map;

import com.bachulun.DAOs.CategoryDAO;
import com.bachulun.DAOs.ICategoryDAO;
import com.bachulun.Models.Category;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;
import com.bachulun.Utils.ValidationUtil;

public class CategoryService implements ICategoryService {
    private final ICategoryDAO cateDao = new CategoryDAO();

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
    public void deleteCategory(int categoryId) throws DatabaseException {
        cateDao.deleteCategory(categoryId);
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
