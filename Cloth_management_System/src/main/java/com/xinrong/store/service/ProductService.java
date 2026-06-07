package com.xinrong.store.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinrong.store.entity.Product;

/**
 * 商品 Service
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品（支持按名称、货号模糊搜索）
     */
    Page<Product> pageQuery(Integer pageNum, Integer pageSize, String name, String productCode);
}
