package com.example.inventory.entity;

import java.time.LocalDateTime;

public class Product {
    private Long id;
    private Long categoryId;
    private String name;
    private String code;
    private String unit;
    private Integer currentStock;
    private Integer safeStock;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}