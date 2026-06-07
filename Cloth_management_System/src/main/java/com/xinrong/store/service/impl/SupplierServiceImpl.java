package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinrong.store.entity.Supplier;
import com.xinrong.store.mapper.SupplierMapper;
import com.xinrong.store.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 供应商 Service 实现
 */
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Override
    public Page<Supplier> pageQuery(Integer pageNum, Integer pageSize, String name) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Supplier::getName, name);
        }
        wrapper.orderByDesc(Supplier::getCreateTime);
        return this.page(page, wrapper);
    }
}
