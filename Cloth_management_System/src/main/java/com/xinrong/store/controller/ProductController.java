package com.xinrong.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.dto.ProductDTO;
import com.xinrong.store.entity.Product;
import com.xinrong.store.entity.Sku;
import com.xinrong.store.service.ProductService;
import com.xinrong.store.service.SkuService;
import com.xinrong.store.vo.ProductVO;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 商品 Controller
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final SkuService skuService;

    public ProductController(ProductService productService, SkuService skuService) {
        this.productService = productService;
        this.skuService = skuService;
    }

    /** 新增商品 */
    @PostMapping
    public Result<Product> create(@Valid @RequestBody ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        if (!StringUtils.hasText(product.getProductCode())) {
            product.setProductCode(null);
        }
        productService.save(product);
        return Result.success(product);
    }

    /** 删除商品（逻辑删除） */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        long skuCount = skuService.count(
                new LambdaQueryWrapper<Sku>().eq(Sku::getProductId, id));
        if (skuCount > 0) {
            throw new IllegalArgumentException("该商品下存在 " + skuCount + " 个规格，请先删除规格后再操作");
        }
        productService.removeById(id);
        return Result.success();
    }

    /** 修改商品 */
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setId(id);
        if (!StringUtils.hasText(product.getProductCode())) {
            product.setProductCode(null);
        }
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
    public Result<Page<ProductVO>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String productCode) {
        return Result.success(productService.pageQuery(pageNum, pageSize, name, productCode));
    }
}
