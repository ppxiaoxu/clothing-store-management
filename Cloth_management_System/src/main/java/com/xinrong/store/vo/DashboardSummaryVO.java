package com.xinrong.store.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 工作台统计概览
 */
@Data
public class DashboardSummaryVO {

    /** 今日销售额（暂返回0） */
    private BigDecimal todaySalesAmount;

    /** 商品总数 */
    private Long productCount;

    /** 库存总数 */
    private Integer stockCount;

    /** 低库存预警（stock <= 2） */
    private Integer lowStockCount;
}
