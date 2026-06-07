package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinrong.store.entity.Sku;
import com.xinrong.store.mapper.ProductMapper;
import com.xinrong.store.mapper.SkuMapper;
import com.xinrong.store.service.DashboardService;
import com.xinrong.store.vo.DashboardSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 工作台统计 Service 实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;

    @Override
    public DashboardSummaryVO getSummary() {
        DashboardSummaryVO vo = new DashboardSummaryVO();

        // 今日销售额（暂未实现销售功能）
        vo.setTodaySalesAmount(BigDecimal.ZERO);

        // 商品总数
        vo.setProductCount(productMapper.selectCount(null));

        // 库存统计
        List<Sku> allSkus = skuMapper.selectList(null);
        int totalStock = 0;
        int lowStock = 0;
        for (Sku sku : allSkus) {
            if (sku.getStock() != null) {
                totalStock += sku.getStock();
                if (sku.getStock() <= 2) {
                    lowStock++;
                }
            }
        }
        vo.setStockCount(totalStock);
        vo.setLowStockCount(lowStock);

        return vo;
    }
}
