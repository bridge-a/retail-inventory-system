package com.example.inventory.mapper;

import com.example.inventory.entity.StockRecord;
import java.util.List;

public interface StockRecordMapper {
    void insert(StockRecord stockRecord);

    List<StockRecord> findByProductId(Long productId);

    List<StockRecord> findAll();
}
