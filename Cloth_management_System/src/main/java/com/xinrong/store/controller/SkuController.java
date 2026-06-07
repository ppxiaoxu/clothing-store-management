package com.xinrong.store.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.entity.Sku;
import com.xinrong.store.service.SkuService;
import org.springframework.web.bind.annotation.*;

/**
 * SKU Controller
 */
@RestController
@RequestMapping("/api/skus")
public class SkuController {

    private final SkuService skuService;

    public SkuController(SkuService skuService) {
        this.skuService = skuService;
    }

    /** 新增SKU */
    @PostMapping
    public Result<Sku> create(@RequestBody Sku sku) {
        skuService.save(sku);
        return Result.success(sku);
    }

    /** 删除SKU（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skuService.removeById(id);
        return Result.success();
    }

    /** 修改SKU */
    @PutMapping("/{id}")
    public Result<Sku> update(@PathVariable Long id, @RequestBody Sku sku) {
        sku.setId(id);
        skuService.updateById(sku);
        return Result.success(sku);
    }

    /** 根据ID查询SKU */
    @GetMapping("/{id}")
    public Result<Sku> getById(@PathVariable Long id) {
        return Result.success(skuService.getById(id));
    }

    /** 分页查询SKU（支持按颜色、尺码搜索） */
    @GetMapping
    public Result<Page<Sku>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size) {
        return Result.success(skuService.pageQuery(pageNum, pageSize, color, size));
    }
}
