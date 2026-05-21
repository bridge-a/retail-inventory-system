package com.example.inventory.controller;

import com.example.inventory.dto.PurchaseApproveRequest;
import com.example.inventory.dto.PurchaseCompleteRequest;
import com.example.inventory.dto.PurchaseCreateRequest;
import com.example.inventory.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    public Long createPurchaseOrder(@RequestBody PurchaseCreateRequest request) {
        return purchaseOrderService.createPurchaseOrder(request);
    }

    @PostMapping("/{id}/approve")
    public void approvePurchaseOrder(@PathVariable Long id, @RequestBody PurchaseApproveRequest request) {
        purchaseOrderService.approvePurchaseOrder(id, request);
    }

    @PostMapping("/{id}/reject")
    public void rejectPurchaseOrder(@PathVariable Long id, @RequestBody PurchaseApproveRequest request) {
        purchaseOrderService.rejectPurchaseOrder(id, request);
    }

    @PostMapping("/{id}/complete")
    public void completePurchaseOrder(@PathVariable Long id, @RequestBody PurchaseCompleteRequest request) {
        purchaseOrderService.completePurchaseOrder(id, request);
    }

    @GetMapping("/{id}")
    public Object getPurchaseOrderDetail(@PathVariable Long id) {
        return purchaseOrderService.getPurchaseOrderDetail(id);
    }

    @GetMapping
    public Object listPurchaseOrders() {
        return purchaseOrderService.listPurchaseOrders();
    }
}
