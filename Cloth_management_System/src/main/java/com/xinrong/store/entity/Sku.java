package com.xinrong.store.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU实体（商品规格）
 */
@Data
@TableName("sku")
public class Sku {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 颜色 */
    private String color;

    /** 尺码 */
    private String size;

    /** 库存数量 */
    private Integer stock;

    /** 进价 */
    private BigDecimal costPrice;

    /** 售价 */
    private BigDecimal salePrice;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
