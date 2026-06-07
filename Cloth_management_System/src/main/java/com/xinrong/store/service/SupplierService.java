package com.xinrong.store.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinrong.store.entity.Supplier;

/**
 * 供应商 Service
 */
public interface SupplierService extends IService<Supplier> {

    /**
     * 分页查询供应商（支持按名称模糊搜索）
     */
    Page<Supplier> pageQuery(Integer pageNum, Integer pageSize, String name);
}
