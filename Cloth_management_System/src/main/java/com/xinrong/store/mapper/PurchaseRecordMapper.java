package com.xinrong.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinrong.store.entity.PurchaseRecord;
import com.xinrong.store.vo.PurchaseRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface PurchaseRecordMapper extends BaseMapper<PurchaseRecord> {

    @Select("""
            <script>
            SELECT
                pr.id,
                pr.supplier_id,
                s.name AS supplier_name,
                pr.product_id,
                p.name AS product_name,
                pr.sku_id,
                sku.color,
                sku.size,
                pr.quantity,
                pr.purchase_price,
                pr.total_amount,
                pr.purchase_time,
                pr.remark,
                pr.created_by,
                u.nickname AS creator_name,
                pr.create_time,
                pr.update_time
            FROM purchase_record pr
            LEFT JOIN supplier s ON pr.supplier_id = s.id
            LEFT JOIN product p ON pr.product_id = p.id
            LEFT JOIN sku ON pr.sku_id = sku.id
            LEFT JOIN sys_user u ON pr.created_by = u.id
            <where>
                <if test="productName != null and productName != ''">
                    AND p.name LIKE CONCAT('%', #{productName}, '%')
                </if>
                <if test="supplierName != null and supplierName != ''">
                    AND s.name LIKE CONCAT('%', #{supplierName}, '%')
                </if>
                <if test="startTime != null">
                    AND pr.purchase_time &gt;= #{startTime}
                </if>
                <if test="endTime != null">
                    AND pr.purchase_time &lt;= #{endTime}
                </if>
            </where>
            ORDER BY pr.purchase_time DESC, pr.id DESC
            </script>
            """)
    Page<PurchaseRecordVO> selectVOPage(Page<PurchaseRecordVO> page,
                                        @Param("productName") String productName,
                                        @Param("supplierName") String supplierName,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Select("""
            SELECT
                pr.id,
                pr.supplier_id,
                s.name AS supplier_name,
                pr.product_id,
                p.name AS product_name,
                pr.sku_id,
                sku.color,
                sku.size,
                pr.quantity,
                pr.purchase_price,
                pr.total_amount,
                pr.purchase_time,
                pr.remark,
                pr.created_by,
                u.nickname AS creator_name,
                pr.create_time,
                pr.update_time
            FROM purchase_record pr
            LEFT JOIN supplier s ON pr.supplier_id = s.id
            LEFT JOIN product p ON pr.product_id = p.id
            LEFT JOIN sku ON pr.sku_id = sku.id
            LEFT JOIN sys_user u ON pr.created_by = u.id
            WHERE pr.id = #{id}
            """)
    PurchaseRecordVO selectVOById(@Param("id") Long id);
}
