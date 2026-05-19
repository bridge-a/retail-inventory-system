package com.example.inventory.controller;

import com.example.inventory.service.StockWarningService;

public class StockWarningController {
    private final StockWarningService stockWarningService;

    public StockWarningController(StockWarningService stockWarningService) {
        this.stockWarningService = stockWarningService;
    }

    public Object listActiveWarnings() {
        return stockWarningService.listActiveWarnings();
    }

    public Object listAllWarnings() {
        return stockWarningService.listAllWarnings();
    }

    public void refreshWarning(Long productId) {
        stockWarningService.refreshWarning(productId);
    }
}
