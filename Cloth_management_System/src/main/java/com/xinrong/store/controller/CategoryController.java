package com.xinrong.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.dto.CategoryDTO;
import com.xinrong.store.entity.Category;
import com.xinrong.store.entity.Product;
import com.xinrong.store.service.CategoryService;
import com.xinrong.store.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 分类 Controller
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public CategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    /** 新增分类 */
    @PostMapping
    public Result<Category> create(@Valid @RequestBody CategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setSortOrder(nextSortOrder(dto.getParentId()));
        categoryService.save(category);
        return Result.success(category);
    }

    /** 删除分类（逻辑删除） */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        long childCount = categoryService.count(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (childCount > 0) {
            throw new IllegalArgumentException("该分类下存在 " + childCount + " 个子分类，请先删除子分类后再操作");
        }
        long productCount = productService.count(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (productCount > 0) {
            throw new IllegalArgumentException("该分类下存在 " + productCount + " 个商品，请先删除商品后再操作");
        }
        categoryService.removeById(id);
        return Result.success();
    }

    /** 修改分类 */
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setSortOrder(dto.getSortOrder().intValue());
        category.setId(id);
        categoryService.updateById(category);
        return Result.success(category);
    }

    /** 根据ID查询分类 */
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /** 分页查询分类 */
    @GetMapping
    public Result<Page<Category>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        return Result.success(categoryService.pageQuery(pageNum, pageSize, name));
    }

    private Integer nextSortOrder(Long parentId) {
        Category lastCategory = categoryService.getOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .orderByDesc(Category::getSortOrder)
                .last("limit 1"));
        return lastCategory == null || lastCategory.getSortOrder() == null ? 1 : lastCategory.getSortOrder() + 1;
    }
}
