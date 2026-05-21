package com.example.inventory.controller;

import com.example.inventory.service.StockWarningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warnings")
public class StockWarningController {
    private final StockWarningService stockWarningService;

    public StockWarningController(StockWarningService stockWarningService) {
        this.stockWarningService = stockWarningService;
    }

    @GetMapping("/active")
    public Object listActiveWarnings() {
        return stockWarningService.listActiveWarnings();
    }

    @GetMapping
    public Object listAllWarnings() {
        return stockWarningService.listAllWarnings();
    }

    @PostMapping("/refresh/{productId}")
    public void refreshWarning(@PathVariable Long productId) {
        stockWarningService.refreshWarning(productId);
    }
}
