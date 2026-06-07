package com.xinrong.store.controller;

import com.xinrong.store.common.Result;
import com.xinrong.store.service.DashboardService;
import com.xinrong.store.vo.DashboardSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台统计 Controller
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** 获取统计概览 */
    @GetMapping("/summary")
    public Result<DashboardSummaryVO> summary() {
        return Result.success(dashboardService.getSummary());
    }
}
