package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinrong.store.dto.PurchaseRecordCreateDTO;
import com.xinrong.store.dto.PurchaseRecordUpdateDTO;
import com.xinrong.store.entity.Product;
import com.xinrong.store.entity.PurchaseRecord;
import com.xinrong.store.entity.Sku;
import com.xinrong.store.mapper.PurchaseRecordMapper;
import com.xinrong.store.mapper.SkuMapper;
import com.xinrong.store.service.ProductService;
import com.xinrong.store.service.PurchaseRecordService;
import com.xinrong.store.service.StockService;
import com.xinrong.store.service.SupplierService;
import com.xinrong.store.vo.PurchaseRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PurchaseRecordServiceImpl extends ServiceImpl<PurchaseRecordMapper, PurchaseRecord> implements PurchaseRecordService {

    private final SupplierService supplierService;
    private final ProductService productService;
    private final SkuMapper skuMapper;
    private final StockService stockService;

    public PurchaseRecordServiceImpl(SupplierService supplierService,
                                     ProductService productService,
                                     SkuMapper skuMapper,
                                     StockService stockService) {
        this.supplierService = supplierService;
        this.productService = productService;
        this.skuMapper = skuMapper;
        this.stockService = stockService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseRecordVO createRecord(PurchaseRecordCreateDTO dto, Long userId) {
        validateRelatedData(dto.getSupplierId(), dto.getProductId(), dto.getSkuId());
        Integer quantity = dto.getQuantity().intValue();

        PurchaseRecord record = new PurchaseRecord();
        record.setSupplierId(dto.getSupplierId());
        record.setProductId(dto.getProductId());
        record.setSkuId(dto.getSkuId());
        record.setQuantity(quantity);
        record.setPurchasePrice(formatMoney(dto.getPurchasePrice()));
        record.setTotalAmount(calculateTotalAmount(quantity, dto.getPurchasePrice()));
        record.setPurchaseTime(dto.getPurchaseTime() == null ? LocalDateTime.now() : dto.getPurchaseTime());
        record.setRemark(dto.getRemark());
        record.setCreatedBy(userId);

        this.save(record);
        stockService.increaseStock(
                record.getSkuId(),
                record.getQuantity(),
                "PURCHASE",
                record.getId(),
                "PURCHASE_RECORD",
                "新增进货记录"
        );
        return getRecordDetail(record.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseRecordVO updateRecord(PurchaseRecordUpdateDTO dto) {
        PurchaseRecord oldRecord = this.getById(dto.getId());
        if (oldRecord == null) {
            throw new IllegalArgumentException("进货记录不存在");
        }
        validateRelatedData(dto.getSupplierId(), dto.getProductId(), dto.getSkuId());
        Integer quantity = dto.getQuantity().intValue();

        if (Objects.equals(oldRecord.getSkuId(), dto.getSkuId())) {
            int diff = quantity - oldRecord.getQuantity();
            if (diff > 0) {
                stockService.increaseStock(
                        dto.getSkuId(),
                        diff,
                        "PURCHASE",
                        dto.getId(),
                        "PURCHASE_RECORD",
                        "修改进货记录增加库存"
                );
            } else if (diff < 0) {
                stockService.decreaseStock(
                        dto.getSkuId(),
                        -diff,
                        "RETURN",
                        dto.getId(),
                        "PURCHASE_RECORD",
                        "修改进货记录回滚库存"
                );
            }
        } else {
            Sku oldSku = skuMapper.selectById(oldRecord.getSkuId());
            int currentStock = oldSku == null || oldSku.getStock() == null ? 0 : oldSku.getStock();
            if (currentStock < oldRecord.getQuantity()) {
                throw new IllegalArgumentException("该进货记录的库存已部分销售，无法更改规格，请先删除相关销售记录后再操作");
            }
            stockService.decreaseStock(
                    oldRecord.getSkuId(),
                    oldRecord.getQuantity(),
                    "RETURN",
                    dto.getId(),
                    "PURCHASE_RECORD",
                    "修改进货记录回滚原规格库存"
            );
            stockService.increaseStock(
                    dto.getSkuId(),
                    quantity,
                    "PURCHASE",
                    dto.getId(),
                    "PURCHASE_RECORD",
                    "修改进货记录增加新规格库存"
            );
        }

        PurchaseRecord record = new PurchaseRecord();
        record.setId(dto.getId());
        record.setSupplierId(dto.getSupplierId());
        record.setProductId(dto.getProductId());
        record.setSkuId(dto.getSkuId());
        record.setQuantity(quantity);
        record.setPurchasePrice(formatMoney(dto.getPurchasePrice()));
        record.setTotalAmount(calculateTotalAmount(quantity, dto.getPurchasePrice()));
        record.setPurchaseTime(dto.getPurchaseTime() == null ? LocalDateTime.now() : dto.getPurchaseTime());
        record.setRemark(dto.getRemark());
        this.updateById(record);
        return getRecordDetail(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        PurchaseRecord record = this.getById(id);
        if (record == null) {
            throw new IllegalArgumentException("进货记录不存在");
        }
        Sku sku = skuMapper.selectById(record.getSkuId());
        int currentStock = sku == null || sku.getStock() == null ? 0 : sku.getStock();
        if (currentStock < record.getQuantity()) {
            throw new IllegalArgumentException("该进货记录的库存已被销售，无法直接删除，请先删除相关销售记录后再操作");
        }
        stockService.decreaseStock(
                record.getSkuId(),
                record.getQuantity(),
                "RETURN",
                record.getId(),
                "PURCHASE_RECORD",
                "删除进货记录回滚库存"
        );
        this.removeById(id);
    }

    @Override
    public PurchaseRecordVO getRecordDetail(Long id) {
        PurchaseRecordVO vo = baseMapper.selectVOById(id);
        if (vo == null) {
            throw new IllegalArgumentException("进货记录不存在");
        }
        return vo;
    }

    @Override
    public Page<PurchaseRecordVO> pageQuery(Integer pageNum,
                                            Integer pageSize,
                                            String productName,
                                            String supplierName,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime) {
        Page<PurchaseRecordVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectVOPage(page, productName, supplierName, startTime, endTime);
    }

    private void validateRelatedData(Long supplierId, Long productId, Long skuId) {
        if (supplierService.getById(supplierId) == null) {
            throw new IllegalArgumentException("进货商不存在");
        }
        Product product = productService.getById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (!Objects.equals(product.getSupplierId(), supplierId)) {
            throw new IllegalArgumentException("该商品不属于当前进货商，请重新选择");
        }
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new IllegalArgumentException("商品规格不存在");
        }
        if (!Objects.equals(sku.getProductId(), product.getId())) {
            throw new IllegalArgumentException("该规格不属于当前商品，请重新选择");
        }
    }

    private BigDecimal calculateTotalAmount(Integer quantity, BigDecimal purchasePrice) {
        return formatMoney(purchasePrice).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal formatMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}
