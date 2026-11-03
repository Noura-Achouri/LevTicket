package com.leviathan.bidpass.mapper;

import com.leviathan.bidpass.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<Category> findAll();
}