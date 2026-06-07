package com.xinrong.store.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinrong.store.dto.PurchaseRecordCreateDTO;
import com.xinrong.store.dto.PurchaseRecordUpdateDTO;
import com.xinrong.store.entity.PurchaseRecord;
import com.xinrong.store.vo.PurchaseRecordVO;

import java.time.LocalDateTime;

public interface PurchaseRecordService extends IService<PurchaseRecord> {

    PurchaseRecordVO createRecord(PurchaseRecordCreateDTO dto);

    PurchaseRecordVO updateRecord(PurchaseRecordUpdateDTO dto);

    void deleteRecord(Long id);

    PurchaseRecordVO getRecordDetail(Long id);

    Page<PurchaseRecordVO> pageQuery(Integer pageNum,
                                     Integer pageSize,
                                     String productName,
                                     String supplierName,
                                     LocalDateTime startTime,
                                     LocalDateTime endTime);
}
