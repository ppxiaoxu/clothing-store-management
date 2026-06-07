package com.xinrong.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_record")
public class PurchaseRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long supplierId;

    private Long productId;

    private Long skuId;

    private Integer quantity;

    private BigDecimal purchasePrice;

    private BigDecimal totalAmount;

    private LocalDateTime purchaseTime;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
