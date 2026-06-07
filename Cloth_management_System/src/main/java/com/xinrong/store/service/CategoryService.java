package com.xinrong.store.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinrong.store.entity.Category;

/**
 * 分类 Service
 */
public interface CategoryService extends IService<Category> {

    /**
     * 分页查询分类（支持按名称模糊搜索）
     */
    Page<Category> pageQuery(Integer pageNum, Integer pageSize, String name);
}
