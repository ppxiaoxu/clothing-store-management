package com.xinrong.store.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseRecordCreateDTO {

    @NotNull(message = "请选择进货商")
    private Long supplierId;

    @NotNull(message = "请选择商品")
    private Long productId;

    @NotNull(message = "请选择商品规格")
    private Long skuId;

    @NotNull(message = "请输入进货数量")
    @Min(value = 1, message = "进货数量必须大于0")
    @Max(value = 999999, message = "进货数量不能超过999999")
    private Long quantity;

    @NotNull(message = "请输入进货单价")
    @DecimalMin(value = "0", message = "进货单价不能小于0")
    @DecimalMax(value = "999999", message = "进货单价不能超过999999")
    private BigDecimal purchasePrice;

    private LocalDateTime purchaseTime;

    private String remark;
}
