package com.example.inventory.service.impl;

import com.example.inventory.dto.PurchaseCreateRequest;
import com.example.inventory.dto.PurchaseItemRequest;
import com.example.inventory.entity.PurchaseOrder;
import com.example.inventory.entity.PurchaseOrderItem;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.mapper.PurchaseOrderItemMapper;
import com.example.inventory.mapper.PurchaseOrderMapper;
import com.example.inventory.service.PurchaseOrderService;
import org.springframework.transaction.annotation.Transactional;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private static final String STATUS_PENDING = "PENDING";

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final ProductMapper productMapper;

    public PurchaseOrderServiceImpl(
            PurchaseOrderMapper purchaseOrderMapper,
            PurchaseOrderItemMapper purchaseOrderItemMapper,
            ProductMapper productMapper
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderItemMapper = purchaseOrderItemMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public Long createPurchaseOrder(PurchaseCreateRequest request) {
        validateCreateRequest(request);

        PurchaseOrder order = new PurchaseOrder();
        order.setApplicantId(request.getApplicantId());
        order.setReason(request.getReason());
        order.setStatus(STATUS_PENDING);

        purchaseOrderMapper.insert(order);

        for (PurchaseItemRequest itemRequest : request.getItems()) {
            validateItem(itemRequest);

            if (productMapper.findById(itemRequest.getProductId()) == null) {
                throw new BusinessException("Product does not exist.");
            }

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            item.setRemark(itemRequest.getRemark());
            purchaseOrderItemMapper.insert(item);
        }

        return order.getId();
    }

    @Override
    public Object getPurchaseOrderDetail(Long id) {
        if (id == null) {
            throw new BusinessException("Purchase order id is required.");
        }

        PurchaseOrder order = purchaseOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException("Purchase order does not exist.");
        }

        return order;
    }

    @Override
    public Object listPurchaseOrders() {
        return purchaseOrderMapper.findAll();
    }

    private void validateCreateRequest(PurchaseCreateRequest request) {
        if (request == null) {
            throw new BusinessException("Purchase request is required.");
        }
        if (request.getApplicantId() == null) {
            throw new BusinessException("Applicant id is required.");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Purchase item list cannot be empty.");
        }
    }

    private void validateItem(PurchaseItemRequest item) {
        if (item == null) {
            throw new BusinessException("Purchase item is required.");
        }
        if (item.getProductId() == null) {
            throw new BusinessException("Product id is required.");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new BusinessException("Purchase quantity must be greater than 0.");
        }
    }
}
