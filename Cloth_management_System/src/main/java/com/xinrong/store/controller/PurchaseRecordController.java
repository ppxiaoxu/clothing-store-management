package com.xinrong.store.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.common.Result;
import com.xinrong.store.dto.PurchaseRecordCreateDTO;
import com.xinrong.store.dto.PurchaseRecordUpdateDTO;
import com.xinrong.store.service.PurchaseRecordService;
import com.xinrong.store.vo.PurchaseRecordVO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/purchase-record")
public class PurchaseRecordController {

    private final PurchaseRecordService purchaseRecordService;

    public PurchaseRecordController(PurchaseRecordService purchaseRecordService) {
        this.purchaseRecordService = purchaseRecordService;
    }

    @PostMapping
    public Result<PurchaseRecordVO> create(@Valid @RequestBody PurchaseRecordCreateDTO dto,
                                           @RequestAttribute("userId") Long userId) {
        return Result.success(purchaseRecordService.createRecord(dto, userId));
    }

    @GetMapping("/page")
    public Result<Page<PurchaseRecordVO>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(purchaseRecordService.pageQuery(pageNum, pageSize, productName, supplierName, startTime, endTime));
    }

    @GetMapping("/{id}")
    public Result<PurchaseRecordVO> getById(@PathVariable Long id) {
        return Result.success(purchaseRecordService.getRecordDetail(id));
    }

    @PutMapping
    public Result<PurchaseRecordVO> update(@Valid @RequestBody PurchaseRecordUpdateDTO dto) {
        return Result.success(purchaseRecordService.updateRecord(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseRecordService.deleteRecord(id);
        return Result.success();
    }
}
