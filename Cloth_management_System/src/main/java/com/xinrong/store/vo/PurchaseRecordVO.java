package com.xinrong.store.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseRecordVO {

    private Long id;

    private Long supplierId;

    private String supplierName;

    private Long productId;

    private String productName;

    private Long skuId;

    private String color;

    private String size;

    private Integer quantity;

    private BigDecimal purchasePrice;

    private BigDecimal totalAmount;

    private LocalDateTime purchaseTime;

    private String remark;

    private Long createdBy;

    private String creatorName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
