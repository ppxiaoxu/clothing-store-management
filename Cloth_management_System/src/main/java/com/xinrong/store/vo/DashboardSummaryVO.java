package com.xinrong.store.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 工作台统计概览
 */
@Data
public class DashboardSummaryVO {

    /** 今日销售额 */
    private BigDecimal todaySalesAmount;

    /** 昨日销售额 */
    private BigDecimal yesterdaySalesAmount;

    /** 环比变化百分比 */
    private BigDecimal changePercent;

    /** 商品总数 */
    private Long productCount;

    /** 库存总数 */
    private Integer stockCount;

    /** 低库存预警（stock <= 2） */
    private Integer lowStockCount;

    /** 零库存或负库存 */
    private Integer zeroStockCount;

    /** 未收款订单数 */
    private Long unpaidOrderCount;

    /** 未收款金额 */
    private BigDecimal unpaidAmount;

    /** 首页待处理事项 */
    private List<DashboardTodoVO> todoItems;

    /** 低库存明细 */
    private List<DashboardStockWarningVO> stockWarnings;

    /** 最近未收款记录 */
    private List<DashboardUnpaidSaleVO> unpaidSales;

    /** 最近操作记录 */
    private List<RecentActivityVO> recentActivities;
}
