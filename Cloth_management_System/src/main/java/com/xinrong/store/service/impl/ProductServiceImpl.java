package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinrong.store.entity.Product;
import com.xinrong.store.mapper.ProductMapper;
import com.xinrong.store.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 商品 Service 实现
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Page<Product> pageQuery(Integer pageNum, Integer pageSize, String name, String productCode) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        if (StringUtils.hasText(productCode)) {
            wrapper.like(Product::getProductCode, productCode);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return this.page(page, wrapper);
    }
}
