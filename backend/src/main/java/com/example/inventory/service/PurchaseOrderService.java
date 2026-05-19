package com.example.inventory.service;

import com.example.inventory.dto.PurchaseApproveRequest;
import com.example.inventory.dto.PurchaseCompleteRequest;
import com.example.inventory.dto.PurchaseCreateRequest;

public interface PurchaseOrderService {
    Long createPurchaseOrder(PurchaseCreateRequest request);

    void approvePurchaseOrder(Long id, PurchaseApproveRequest request);

    void rejectPurchaseOrder(Long id, PurchaseApproveRequest request);

    void completePurchaseOrder(Long id, PurchaseCompleteRequest request);

    Object getPurchaseOrderDetail(Long id);

    Object listPurchaseOrders();
}
