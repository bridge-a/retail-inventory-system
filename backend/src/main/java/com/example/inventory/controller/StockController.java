package com.example.inventory.controller;

import com.example.inventory.dto.StockInRequest;
import com.example.inventory.dto.StockOutRequest;
import com.example.inventory.service.StockService;

public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    public void stockIn(StockInRequest request) {
        stockService.stockIn(request);
    }

    public void stockOut(StockOutRequest request) {
        stockService.stockOut(request);
    }

    public Object listStockRecords() {
        return stockService.listStockRecords();
    }

    public Object listStockRecordsByProduct(Long productId) {
        return stockService.listStockRecordsByProduct(productId);
    }
}
