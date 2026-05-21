package com.example.inventory.controller;

import com.example.inventory.dto.StockInRequest;
import com.example.inventory.dto.StockOutRequest;
import com.example.inventory.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/in")
    public void stockIn(@RequestBody StockInRequest request) {
        stockService.stockIn(request);
    }

    @PostMapping("/out")
    public void stockOut(@RequestBody StockOutRequest request) {
        stockService.stockOut(request);
    }

    @GetMapping("/records")
    public Object listStockRecords() {
        return stockService.listStockRecords();
    }

    @GetMapping("/records/product/{productId}")
    public Object listStockRecordsByProduct(@PathVariable Long productId) {
        return stockService.listStockRecordsByProduct(productId);
    }
}
