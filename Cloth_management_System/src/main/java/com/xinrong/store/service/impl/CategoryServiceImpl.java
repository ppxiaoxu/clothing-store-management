package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinrong.store.entity.Category;
import com.xinrong.store.mapper.CategoryMapper;
import com.xinrong.store.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 分类 Service 实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public Page<Category> pageQuery(Integer pageNum, Integer pageSize, String name) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        // 按名称模糊搜索
        if (StringUtils.hasText(name)) {
            wrapper.like(Category::getName, name);
        }
        // 按排序字段升序
        wrapper.orderByAsc(Category::getSortOrder);
        return this.page(page, wrapper);
    }
}
