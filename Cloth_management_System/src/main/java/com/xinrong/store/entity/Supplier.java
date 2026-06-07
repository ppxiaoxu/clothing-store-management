package com.xinrong.store.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商实体
 */
@Data
@TableName("supplier")
public class Supplier {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 供应商名称 */
    private String name;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 备注 */
    private String remark;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
