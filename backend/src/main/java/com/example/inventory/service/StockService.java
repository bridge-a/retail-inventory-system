package com.example.inventory.service;

import com.example.inventory.dto.StockInRequest;
import com.example.inventory.dto.StockOutRequest;

public interface StockService {
    void stockIn(StockInRequest request);

    void stockOut(StockOutRequest request);

    Object listStockRecords();

    Object listStockRecordsByProduct(Long productId);
}
