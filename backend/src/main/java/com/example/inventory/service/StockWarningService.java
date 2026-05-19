package com.example.inventory.service;

public interface StockWarningService {
    void refreshWarning(Long productId);

    Object listActiveWarnings();

    Object listAllWarnings();
}
