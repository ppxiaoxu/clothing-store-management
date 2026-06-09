package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinrong.store.entity.Product;
import com.xinrong.store.entity.Supplier;
import com.xinrong.store.mapper.ProductMapper;
import com.xinrong.store.service.ProductService;
import com.xinrong.store.service.SupplierService;
import com.xinrong.store.vo.ProductVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品 Service 实现
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final SupplierService supplierService;

    public ProductServiceImpl(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Override
    public Page<ProductVO> pageQuery(Integer pageNum, Integer pageSize, String name, String productCode) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        if (StringUtils.hasText(productCode)) {
            wrapper.like(Product::getProductCode, productCode);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> productPage = this.page(page, wrapper);
        Map<Long, String> supplierNameMap = loadSupplierNameMap(productPage);

        Page<ProductVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        voPage.setRecords(productPage.getRecords().stream()
                .map(product -> ProductVO.from(product, supplierNameMap.get(product.getSupplierId())))
                .toList());
        return voPage;
    }

    private Map<Long, String> loadSupplierNameMap(Page<Product> productPage) {
        Set<Long> supplierIds = productPage.getRecords().stream()
                .map(Product::getSupplierId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (supplierIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return supplierService.listByIds(supplierIds).stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getName, (left, right) -> left));
    }
}
