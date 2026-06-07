package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinrong.store.entity.Sku;
import com.xinrong.store.mapper.SkuMapper;
import com.xinrong.store.service.SkuService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * SKU Service 实现
 */
@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {

    @Override
    public Page<Sku> pageQuery(Integer pageNum, Integer pageSize, String color, String size) {
        Page<Sku> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(color)) {
            wrapper.like(Sku::getColor, color);
        }
        if (StringUtils.hasText(size)) {
            wrapper.eq(Sku::getSize, size);
        }
        wrapper.orderByAsc(Sku::getColor, Sku::getSize);
        return this.page(page, wrapper);
    }
}
