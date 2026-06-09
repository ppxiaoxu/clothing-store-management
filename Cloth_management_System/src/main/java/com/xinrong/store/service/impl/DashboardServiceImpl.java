package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinrong.store.entity.Sku;
import com.xinrong.store.entity.StockRecord;
import com.xinrong.store.mapper.ProductMapper;
import com.xinrong.store.mapper.SaleRecordMapper;
import com.xinrong.store.mapper.SkuMapper;
import com.xinrong.store.mapper.StockRecordMapper;
import com.xinrong.store.service.DashboardService;
import com.xinrong.store.service.StatisticsService;
import com.xinrong.store.vo.DashboardSummaryVO;
import com.xinrong.store.vo.DashboardTodoVO;
import com.xinrong.store.vo.RecentActivityVO;
import com.xinrong.store.vo.SalesSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 工作台统计 Service 实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final SaleRecordMapper saleRecordMapper;
    private final StockRecordMapper stockRecordMapper;
    private final StatisticsService statisticsService;

    @Override
    public DashboardSummaryVO getSummary() {
        DashboardSummaryVO vo = new DashboardSummaryVO();

        // 今日销售额
        BigDecimal todaySales = statisticsService.getTodaySales().getTotalSalesAmount();
        vo.setTodaySalesAmount(todaySales);

        // 昨日销售额及环比
        LocalDate yesterday = LocalDate.now().minusDays(1);
        BigDecimal yesterdaySales = statisticsService.getSalesSummary(yesterday, yesterday).getTotalSalesAmount();
        vo.setYesterdaySalesAmount(yesterdaySales);
        if (yesterdaySales.compareTo(BigDecimal.ZERO) > 0) {
            vo.setChangePercent(todaySales.subtract(yesterdaySales)
                    .divide(yesterdaySales, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP));
        } else {
            vo.setChangePercent(todaySales.compareTo(BigDecimal.ZERO) > 0 ? null : BigDecimal.ZERO);
        }

        // 商品总数
        vo.setProductCount(productMapper.selectCount(null));

        // 库存统计
        List<Sku> allSkus = skuMapper.selectAllWithActiveProduct();
        int totalStock = 0;
        int lowStock = 0;
        int zeroStock = 0;
        for (Sku sku : allSkus) {
            if (sku.getStock() != null) {
                totalStock += sku.getStock();
                if (sku.getStock() <= 2) {
                    lowStock++;
                }
                if (sku.getStock() <= 0) {
                    zeroStock++;
                }
            }
        }
        vo.setStockCount(totalStock);
        vo.setLowStockCount(lowStock);
        vo.setZeroStockCount(zeroStock);
        vo.setStockWarnings(skuMapper.selectStockWarnings(2, 8));

        SalesSummaryVO allSales = statisticsService.getSalesSummary(null, null);
        Long unpaidOrderCount = saleRecordMapper.countUnpaidSales();
        vo.setUnpaidOrderCount(unpaidOrderCount == null ? 0L : unpaidOrderCount);
        vo.setUnpaidAmount(allSales.getUnpaidSalesAmount());
        vo.setUnpaidSales(saleRecordMapper.selectRecentUnpaidSales(6));
        vo.setTodoItems(buildTodoItems(lowStock, zeroStock, vo.getUnpaidOrderCount()));

        // 最近操作记录
        Map<String, String> changeTypeLabel = Map.of(
                "PURCHASE", "进货", "SALE", "销售", "RETURN", "回退",
                "ADJUST", "调整", "手动记录", "手动"
        );
        List<StockRecord> recentRecords = stockRecordMapper.selectList(
                new LambdaQueryWrapper<StockRecord>()
                        .orderByDesc(StockRecord::getCreateTime)
                        .last("LIMIT 10"));
        List<RecentActivityVO> activities = new ArrayList<>();
        for (StockRecord r : recentRecords) {
            RecentActivityVO activity = new RecentActivityVO();
            activity.setContent(r.getReason());
            activity.setTime(r.getCreateTime());
            activity.setOperator(changeTypeLabel.getOrDefault(r.getChangeType(), r.getChangeType()));
            activities.add(activity);
        }
        vo.setRecentActivities(activities);

        return vo;
    }

    private List<DashboardTodoVO> buildTodoItems(int lowStock, int zeroStock, long unpaidOrderCount) {
        List<DashboardTodoVO> items = new ArrayList<>();
        if (zeroStock > 0) {
            items.add(todo("零库存商品", "优先确认是否需要补货或下架", zeroStock, "/sku", "danger"));
        }
        if (lowStock > 0) {
            items.add(todo("低库存预警", "库存不高于 2 件的规格需要关注", lowStock, "/sku", "warning"));
        }
        if (unpaidOrderCount > 0) {
            items.add(todo("未收款销售", "及时核对收款状态，避免漏账", (int) unpaidOrderCount, "/sale-record", "warning"));
        }
        if (items.isEmpty()) {
            items.add(todo("暂无紧急事项", "库存和收款状态正常，可以继续录入日常业务", 0, "/dashboard", "success"));
        }
        return items;
    }

    private DashboardTodoVO todo(String title, String description, int count, String path, String severity) {
        DashboardTodoVO item = new DashboardTodoVO();
        item.setTitle(title);
        item.setDescription(description);
        item.setCount(count);
        item.setPath(path);
        item.setSeverity(severity);
        return item;
    }
}
