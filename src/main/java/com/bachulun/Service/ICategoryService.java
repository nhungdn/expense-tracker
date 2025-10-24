package com.bachulun.Service;

import java.util.List;
import java.util.Map;

import com.bachulun.Models.Category;
import com.bachulun.Utils.DatabaseException;
import com.bachulun.Utils.InvalidInputException;

public interface ICategoryService {
    void addCategory(Category category) throws InvalidInputException, DatabaseException;

    void updateCategory(int categoryId, String newName) throws InvalidInputException, DatabaseException;

    void deleteCategory(int categoryId) throws DatabaseException;

    List<Category> getCategoryByUserId(int userId) throws DatabaseException;

    String getCategoryNameByCategoryId(int categoryId) throws DatabaseException;

    Map<String, Integer> getAllCategoryIdAndNameByUserId(int userId) throws DatabaseException;
}
