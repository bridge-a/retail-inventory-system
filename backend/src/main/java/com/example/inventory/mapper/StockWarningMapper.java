package com.example.inventory.mapper;

import com.example.inventory.entity.StockWarning;
import java.util.List;

public interface StockWarningMapper {
    StockWarning findByProductId(Long productId);

    void insert(StockWarning warning);

    void update(StockWarning warning);

    List<StockWarning> findActiveWarnings();

    List<StockWarning> findAll();
}
