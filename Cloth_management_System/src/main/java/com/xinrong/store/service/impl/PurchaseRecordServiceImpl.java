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

    public PurchaseRecordServiceImpl(SupplierService supplierService,
                                     ProductService productService,
                                     SkuMapper skuMapper) {
        this.supplierService = supplierService;
        this.productService = productService;
        this.skuMapper = skuMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseRecordVO createRecord(PurchaseRecordCreateDTO dto) {
        validateRelatedData(dto.getSupplierId(), dto.getProductId(), dto.getSkuId());

        PurchaseRecord record = new PurchaseRecord();
        record.setSupplierId(dto.getSupplierId());
        record.setProductId(dto.getProductId());
        record.setSkuId(dto.getSkuId());
        record.setQuantity(dto.getQuantity());
        record.setPurchasePrice(formatMoney(dto.getPurchasePrice()));
        record.setTotalAmount(calculateTotalAmount(dto.getQuantity(), dto.getPurchasePrice()));
        record.setPurchaseTime(dto.getPurchaseTime() == null ? LocalDateTime.now() : dto.getPurchaseTime());
        record.setRemark(dto.getRemark());

        this.save(record);
        int changed = skuMapper.increaseStock(record.getSkuId(), record.getQuantity());
        if (changed == 0) {
            throw new IllegalArgumentException("商品规格不存在，无法增加库存");
        }
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

        if (Objects.equals(oldRecord.getSkuId(), dto.getSkuId())) {
            int diff = dto.getQuantity() - oldRecord.getQuantity();
            if (diff > 0) {
                skuMapper.increaseStock(dto.getSkuId(), diff);
            } else if (diff < 0) {
                decreaseStockOrThrow(dto.getSkuId(), -diff, "当前库存不足，无法修改该进货记录");
            }
        } else {
            decreaseStockOrThrow(oldRecord.getSkuId(), oldRecord.getQuantity(), "当前库存不足，无法修改该进货记录");
            skuMapper.increaseStock(dto.getSkuId(), dto.getQuantity());
        }

        PurchaseRecord record = new PurchaseRecord();
        record.setId(dto.getId());
        record.setSupplierId(dto.getSupplierId());
        record.setProductId(dto.getProductId());
        record.setSkuId(dto.getSkuId());
        record.setQuantity(dto.getQuantity());
        record.setPurchasePrice(formatMoney(dto.getPurchasePrice()));
        record.setTotalAmount(calculateTotalAmount(dto.getQuantity(), dto.getPurchasePrice()));
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
        decreaseStockOrThrow(record.getSkuId(), record.getQuantity(), "当前库存不足，无法删除该进货记录");
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
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new IllegalArgumentException("商品规格不存在");
        }
        if (!Objects.equals(sku.getProductId(), product.getId())) {
            throw new IllegalArgumentException("商品规格不属于所选商品");
        }
    }

    private void decreaseStockOrThrow(Long skuId, Integer quantity, String message) {
        int changed = skuMapper.decreaseStock(skuId, quantity);
        if (changed == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private BigDecimal calculateTotalAmount(Integer quantity, BigDecimal purchasePrice) {
        return formatMoney(purchasePrice).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal formatMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}
