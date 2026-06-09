package com.xinrong.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.dto.SupplierDTO;
import com.xinrong.store.entity.Product;
import com.xinrong.store.entity.PurchaseRecord;
import com.xinrong.store.entity.Supplier;
import com.xinrong.store.service.ProductService;
import com.xinrong.store.service.PurchaseRecordService;
import com.xinrong.store.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商 Controller
 */
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final ProductService productService;
    private final PurchaseRecordService purchaseRecordService;

    public SupplierController(SupplierService supplierService,
                              ProductService productService,
                              PurchaseRecordService purchaseRecordService) {
        this.supplierService = supplierService;
        this.productService = productService;
        this.purchaseRecordService = purchaseRecordService;
    }

    /** 新增供应商 */
    @PostMapping
    public Result<Supplier> create(@Valid @RequestBody SupplierDTO dto) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        supplierService.save(supplier);
        return Result.success(supplier);
    }

    /** 删除供应商（逻辑删除） */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        Supplier supplier = supplierService.getById(id);
        if (supplier == null) {
            throw new IllegalArgumentException("进货商不存在");
        }
        long productCount = productService.count(
                new LambdaQueryWrapper<Product>().eq(Product::getSupplierId, id));
        if (productCount > 0) {
            throw new IllegalArgumentException("该进货商下存在 " + productCount + " 个商品，请先删除商品后再操作");
        }
        long purchaseCount = purchaseRecordService.count(
                new LambdaQueryWrapper<PurchaseRecord>().eq(PurchaseRecord::getSupplierId, id));
        if (purchaseCount > 0) {
            throw new IllegalArgumentException("该进货商下存在 " + purchaseCount + " 条进货记录，请先删除相关记录后再操作");
        }
        supplierService.removeById(id);
        return Result.success();
    }

    /** 修改供应商 */
    @PutMapping("/{id}")
    public Result<Supplier> update(@PathVariable Long id, @Valid @RequestBody SupplierDTO dto) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
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
