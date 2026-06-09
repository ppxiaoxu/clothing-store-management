package com.xinrong.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.dto.SkuDTO;
import com.xinrong.store.entity.PurchaseRecord;
import com.xinrong.store.entity.SaleRecord;
import com.xinrong.store.entity.Sku;
import com.xinrong.store.service.PurchaseRecordService;
import com.xinrong.store.service.SaleRecordService;
import com.xinrong.store.service.SkuService;
import com.xinrong.store.service.StockService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * SKU Controller
 */
@RestController
@RequestMapping("/api/skus")
public class SkuController {

    private final SkuService skuService;
    private final StockService stockService;
    private final PurchaseRecordService purchaseRecordService;
    private final SaleRecordService saleRecordService;

    public SkuController(SkuService skuService,
                         StockService stockService,
                         PurchaseRecordService purchaseRecordService,
                         SaleRecordService saleRecordService) {
        this.skuService = skuService;
        this.stockService = stockService;
        this.purchaseRecordService = purchaseRecordService;
        this.saleRecordService = saleRecordService;
    }

    /** 新增SKU */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Sku> create(@Valid @RequestBody SkuDTO dto) {
        checkSkuDuplicate(dto.getProductId(), dto.getColor(), dto.getSize(), null);
        Sku sku = new Sku();
        BeanUtils.copyProperties(dto, sku);
        int initialStock = dto.getStock().intValue();
        sku.setStock(0);
        skuService.save(sku);
        if (initialStock > 0) {
            stockService.increaseStock(
                    sku.getId(),
                    initialStock,
                    "手动记录",
                    sku.getId(),
                    "SKU",
                    "初始库存录入"
            );
        }
        sku.setStock(initialStock);
        return Result.success(sku);
    }

    /** 删除SKU（逻辑删除） */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        Sku sku = skuService.getById(id);
        if (sku == null) {
            throw new IllegalArgumentException("商品规格不存在");
        }
        long purchaseCount = purchaseRecordService.count(
                new LambdaQueryWrapper<PurchaseRecord>().eq(PurchaseRecord::getSkuId, id));
        long saleCount = saleRecordService.count(
                new LambdaQueryWrapper<SaleRecord>().eq(SaleRecord::getSkuId, id));
        if (purchaseCount > 0 || saleCount > 0) {
            throw new IllegalArgumentException("该规格存在关联的进货或销售记录，请先删除相关记录后再操作");
        }
        skuService.removeById(id);
        return Result.success();
    }

    /** 修改SKU */
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Sku> update(@PathVariable Long id, @Valid @RequestBody SkuDTO dto) {
        Sku oldSku = skuService.getById(id);
        if (oldSku == null) {
            throw new IllegalArgumentException("商品规格不存在");
        }
        checkSkuDuplicate(dto.getProductId(), dto.getColor(), dto.getSize(), id);
        Sku sku = new Sku();
        BeanUtils.copyProperties(dto, sku);
        sku.setStock(dto.getStock().intValue());
        sku.setId(id);
        Integer adjustedStock = null;
        if (sku.getStock() != null && !Objects.equals(sku.getStock(), oldSku.getStock())) {
            adjustedStock = sku.getStock();
            stockService.adjustStock(id, sku.getStock(), "SKU信息编辑调整库存");
            sku.setStock(null);
        }
        skuService.updateById(sku);
        if (adjustedStock != null) {
            sku.setStock(adjustedStock);
        }
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

    private void checkSkuDuplicate(Long productId, String color, String size, Long excludeId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<Sku>()
                .eq(Sku::getProductId, productId)
                .eq(Sku::getColor, color)
                .eq(Sku::getSize, size);
        if (excludeId != null) {
            wrapper.ne(Sku::getId, excludeId);
        }
        if (skuService.count(wrapper) > 0) {
            throw new IllegalArgumentException("该商品下已存在相同颜色和尺码的规格");
        }
    }
}
