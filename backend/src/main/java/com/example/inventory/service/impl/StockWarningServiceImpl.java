package com.example.inventory.service.impl;

import com.example.inventory.entity.Product;
import com.example.inventory.entity.StockWarning;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.mapper.StockWarningMapper;
import com.example.inventory.service.StockWarningService;

import java.time.LocalDateTime;

public class StockWarningServiceImpl implements StockWarningService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_RESOLVED = "RESOLVED";

    private final ProductMapper productMapper;
    private final StockWarningMapper stockWarningMapper;

    public StockWarningServiceImpl(ProductMapper productMapper, StockWarningMapper stockWarningMapper) {
        this.productMapper = productMapper;
        this.stockWarningMapper = stockWarningMapper;
    }

    @Override
    public void refreshWarning(Long productId) {
        if (productId == null) {
            throw new BusinessException("Product id is required.");
        }

        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException("Product does not exist.");
        }

        int currentStock = safeNumber(product.getCurrentStock());
        int safeStock = safeNumber(product.getSafeStock());

        if (safeStock <= 0) {
            resolveExistingWarning(productId, currentStock, safeStock);
            return;
        }

        StockWarning warning = stockWarningMapper.findByProductId(productId);
        if (currentStock < safeStock) {
            activateWarning(productId, currentStock, safeStock, warning);
        } else if (warning != null && STATUS_ACTIVE.equals(warning.getStatus())) {
            resolveWarning(warning, currentStock, safeStock);
        }
    }

    @Override
    public Object listActiveWarnings() {
        return stockWarningMapper.findActiveWarnings();
    }

    @Override
    public Object listAllWarnings() {
        return stockWarningMapper.findAll();
    }

    private void resolveExistingWarning(Long productId, int currentStock, int safeStock) {
        StockWarning warning = stockWarningMapper.findByProductId(productId);
        if (warning != null && STATUS_ACTIVE.equals(warning.getStatus())) {
            resolveWarning(warning, currentStock, safeStock);
        }
    }

    private void activateWarning(Long productId, int currentStock, int safeStock, StockWarning warning) {
        if (warning == null) {
            StockWarning newWarning = new StockWarning();
            newWarning.setProductId(productId);
            newWarning.setWarningStock(currentStock);
            newWarning.setSafeStock(safeStock);
            newWarning.setStatus(STATUS_ACTIVE);
            newWarning.setCreatedAt(LocalDateTime.now());
            newWarning.setUpdatedAt(LocalDateTime.now());
            stockWarningMapper.insert(newWarning);
            return;
        }

        warning.setWarningStock(currentStock);
        warning.setSafeStock(safeStock);
        warning.setStatus(STATUS_ACTIVE);
        warning.setUpdatedAt(LocalDateTime.now());
        stockWarningMapper.update(warning);
    }

    private void resolveWarning(StockWarning warning, int currentStock, int safeStock) {
        warning.setWarningStock(currentStock);
        warning.setSafeStock(safeStock);
        warning.setStatus(STATUS_RESOLVED);
        warning.setUpdatedAt(LocalDateTime.now());
        stockWarningMapper.update(warning);
    }

    private int safeNumber(Integer value) {
        return value == null ? 0 : value;
    }
}