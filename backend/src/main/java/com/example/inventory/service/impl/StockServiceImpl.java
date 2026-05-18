package com.example.inventory.service.impl;

import com.example.inventory.dto.StockInRequest;
import com.example.inventory.dto.StockOutRequest;
import com.example.inventory.service.StockService;

public class StockServiceImpl implements StockService {
    @Override
    public void stockIn(StockInRequest request) {
        // TODO: Increase product.currentStock and create stock_record.
    }

    @Override
    public void stockOut(StockOutRequest request) {
        // TODO: Check stock cannot be negative and create stock_record.
    }

    @Override
    public Object listStockRecords() {
        return null;
    }

    @Override
    public Object listStockRecordsByProduct(Long productId) {
        return null;
    }
}
