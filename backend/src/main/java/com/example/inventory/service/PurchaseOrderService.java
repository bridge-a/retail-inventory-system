package com.example.inventory.service;

import com.example.inventory.dto.PurchaseCreateRequest;

public interface PurchaseOrderService {
    Long createPurchaseOrder(PurchaseCreateRequest request);

    Object getPurchaseOrderDetail(Long id);

    Object listPurchaseOrders();
}
