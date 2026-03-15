package com.campus.forum.service;

import com.campus.forum.entity.Category;
import java.util.List;

public interface CategoryService {
    /**
     * 查询所有分类列表
     */
    List<Category> getAllCategories();
}