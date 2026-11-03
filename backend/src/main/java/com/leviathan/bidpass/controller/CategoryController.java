package com.leviathan.bidpass.controller;

import com.leviathan.bidpass.entity.Category;
import com.leviathan.bidpass.mapper.CategoryMapper;
import com.leviathan.bidpass.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping
    public Result<List<Category>> getCategories() {
        List<Category> categories = categoryMapper.findAll();
        return Result.success(categories);
    }
}