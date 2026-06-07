package com.xinrong.store.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.entity.Category;
import com.xinrong.store.service.CategoryService;
import org.springframework.web.bind.annotation.*;

/**
 * 分类 Controller
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** 新增分类 */
    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success(category);
    }

    /** 删除分类（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }

    /** 修改分类 */
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
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
}
