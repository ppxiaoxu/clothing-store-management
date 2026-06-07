package com.xinrong.store.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.entity.Supplier;
import com.xinrong.store.service.SupplierService;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商 Controller
 */
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /** 新增供应商 */
    @PostMapping
    public Result<Supplier> create(@RequestBody Supplier supplier) {
        supplierService.save(supplier);
        return Result.success(supplier);
    }

    /** 删除供应商（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.removeById(id);
        return Result.success();
    }

    /** 修改供应商 */
    @PutMapping("/{id}")
    public Result<Supplier> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        supplierService.updateById(supplier);
        return Result.success(supplier);
    }

    /** 根据ID查询供应商 */
    @GetMapping("/{id}")
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    /** 分页查询供应商 */
    @GetMapping
    public Result<Page<Supplier>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        return Result.success(supplierService.pageQuery(pageNum, pageSize, name));
    }
}
