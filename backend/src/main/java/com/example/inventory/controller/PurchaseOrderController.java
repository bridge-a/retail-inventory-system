package com.example.inventory.controller;

import com.example.inventory.dto.PurchaseApproveRequest;
import com.example.inventory.dto.PurchaseCompleteRequest;
import com.example.inventory.dto.PurchaseCreateRequest;
import com.example.inventory.service.PurchaseOrderService;

public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public Long createPurchaseOrder(PurchaseCreateRequest request) {
        return purchaseOrderService.createPurchaseOrder(request);
    }

    public void approvePurchaseOrder(Long id, PurchaseApproveRequest request) {
        purchaseOrderService.approvePurchaseOrder(id, request);
    }

    public void rejectPurchaseOrder(Long id, PurchaseApproveRequest request) {
        purchaseOrderService.rejectPurchaseOrder(id, request);
    }

    public void completePurchaseOrder(Long id, PurchaseCompleteRequest request) {
        purchaseOrderService.completePurchaseOrder(id, request);
    }

    public Object getPurchaseOrderDetail(Long id) {
        return purchaseOrderService.getPurchaseOrderDetail(id);
    }

    public Object listPurchaseOrders() {
        return purchaseOrderService.listPurchaseOrders();
    }
}
