package com.xinrong.store.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinrong.store.entity.Sku;

/**
 * SKU Service
 */
public interface SkuService extends IService<Sku> {

    /**
     * 分页查询SKU（支持按颜色、尺码模糊搜索）
     */
    Page<Sku> pageQuery(Integer pageNum, Integer pageSize, String color, String size);
}
