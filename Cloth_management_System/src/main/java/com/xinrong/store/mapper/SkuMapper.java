package com.xinrong.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinrong.store.entity.Sku;
import com.xinrong.store.vo.DashboardStockWarningVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * SKU Mapper
 */
@Mapper
public interface SkuMapper extends BaseMapper<Sku> {

    @Select("SELECT * FROM sku WHERE id = #{skuId} AND deleted = 0 FOR UPDATE")
    Sku selectByIdForUpdate(@Param("skuId") Long skuId);

    @Update("UPDATE sku SET stock = stock + #{quantity}, update_time = NOW() WHERE id = #{skuId} AND deleted = 0")
    int increaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("UPDATE sku SET stock = stock - #{quantity}, update_time = NOW() WHERE id = #{skuId} AND deleted = 0 AND stock >= #{quantity}")
    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("UPDATE sku SET stock = #{newStock}, update_time = NOW() WHERE id = #{skuId} AND deleted = 0")
    int adjustStock(@Param("skuId") Long skuId, @Param("newStock") Integer newStock);

    @Select("""
            SELECT sku.*
            FROM sku
            INNER JOIN product ON sku.product_id = product.id AND product.deleted = 0
            WHERE sku.deleted = 0
            """)
    List<Sku> selectAllWithActiveProduct();

    @Select("""
            SELECT
                sku.id AS sku_id,
                sku.product_id,
                product.name AS product_name,
                sku.color,
                sku.size,
                sku.stock,
                sku.sale_price,
                CASE WHEN sku.stock <= 0 THEN 'danger' ELSE 'warning' END AS warning_level
            FROM sku
            INNER JOIN product ON sku.product_id = product.id AND product.deleted = 0
            WHERE sku.deleted = 0 AND sku.stock <= #{threshold}
            ORDER BY sku.stock ASC, sku.update_time DESC
            LIMIT #{limit}
            """)
    List<DashboardStockWarningVO> selectStockWarnings(@Param("threshold") Integer threshold,
                                                      @Param("limit") Integer limit);
}
