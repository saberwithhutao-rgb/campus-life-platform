package com.campus.forum.service.impl;

import com.campus.forum.entity.Category;
import com.campus.forum.repository.CategoryRepository;
import com.campus.forum.service.CategoryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        try {
            return categoryRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询分类失败: " + e.getMessage());
        }
    }
}