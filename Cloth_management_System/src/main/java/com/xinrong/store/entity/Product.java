package com.xinrong.store.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 货号 */
    private String productCode;

    /** 分类ID */
    private Long categoryId;

    /** 供应商ID */
    private Long supplierId;

    /** 性别: 0-通用, 1-男童, 2-女童 */
    private Integer gender;

    /** 适合年龄 */
    private String suitableAge;

    /** 季节 */
    private String season;

    /** 图片地址 */
    private String imageUrl;

    /** 备注 */
    private String remark;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
