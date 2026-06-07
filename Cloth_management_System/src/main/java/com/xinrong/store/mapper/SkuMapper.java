package com.xinrong.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinrong.store.entity.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * SKU Mapper
 */
@Mapper
public interface SkuMapper extends BaseMapper<Sku> {

    @Update("UPDATE sku SET stock = stock + #{quantity}, update_time = NOW() WHERE id = #{skuId} AND deleted = 0")
    int increaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("UPDATE sku SET stock = stock - #{quantity}, update_time = NOW() WHERE id = #{skuId} AND deleted = 0 AND stock >= #{quantity}")
    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
