package com.xinrong.store.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.entity.Product;
import com.xinrong.store.service.ProductService;
import org.springframework.web.bind.annotation.*;

/**
 * 商品 Controller
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** 新增商品 */
    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        productService.save(product);
        return Result.success(product);
    }

    /** 删除商品（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }

    /** 修改商品 */
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.updateById(product);
        return Result.success(product);
    }

    /** 根据ID查询商品 */
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /** 分页查询商品（支持按名称、货号搜索） */
    @GetMapping
    public Result<Page<Product>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String productCode) {
        return Result.success(productService.pageQuery(pageNum, pageSize, name, productCode));
    }
}
