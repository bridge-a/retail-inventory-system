package com.example.inventory.service.impl;

import com.example.inventory.dto.PurchaseApproveRequest;
import com.example.inventory.dto.PurchaseCompleteRequest;
import com.example.inventory.dto.PurchaseCreateRequest;
import com.example.inventory.dto.PurchaseItemRequest;
import com.example.inventory.dto.StockInRequest;
import com.example.inventory.entity.PurchaseOrder;
import com.example.inventory.entity.PurchaseOrderItem;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.mapper.PurchaseOrderItemMapper;
import com.example.inventory.mapper.PurchaseOrderMapper;
import com.example.inventory.service.AuthService;
import com.example.inventory.service.PurchaseOrderService;
import com.example.inventory.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final ProductMapper productMapper;
    private final StockService stockService;
    private final AuthService authService;

    public PurchaseOrderServiceImpl(
            PurchaseOrderMapper purchaseOrderMapper,
            PurchaseOrderItemMapper purchaseOrderItemMapper,
            ProductMapper productMapper,
            StockService stockService,
            AuthService authService
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderItemMapper = purchaseOrderItemMapper;
        this.productMapper = productMapper;
        this.stockService = stockService;
        this.authService = authService;
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
    public void approvePurchaseOrder(Long id, PurchaseApproveRequest request) {
        validateApproveRequest(request);
        authService.ensureAdmin(request.getApproverId());

        PurchaseOrder order = getExistingOrder(id);
        ensureStatus(order, STATUS_PENDING);

        order.setStatus(STATUS_APPROVED);
        order.setApproverId(request.getApproverId());
        order.setApprovalRemark(request.getApprovalRemark());
        order.setApprovedAt(LocalDateTime.now());

        purchaseOrderMapper.update(order);
    }

    @Override
    public void rejectPurchaseOrder(Long id, PurchaseApproveRequest request) {
        validateApproveRequest(request);
        authService.ensureAdmin(request.getApproverId());

        PurchaseOrder order = getExistingOrder(id);
        ensureStatus(order, STATUS_PENDING);

        order.setStatus(STATUS_REJECTED);
        order.setApproverId(request.getApproverId());
        order.setApprovalRemark(request.getApprovalRemark());
        order.setApprovedAt(LocalDateTime.now());

        purchaseOrderMapper.update(order);
    }

    @Override
    @Transactional
    public void completePurchaseOrder(Long id, PurchaseCompleteRequest request) {
        validateCompleteRequest(request);

        PurchaseOrder order = getExistingOrder(id);
        ensureStatus(order, STATUS_APPROVED);

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.findByOrderId(id);
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Purchase order item list cannot be empty.");
        }

        for (PurchaseOrderItem item : items) {
            StockInRequest stockInRequest = new StockInRequest();
            stockInRequest.setProductId(item.getProductId());
            stockInRequest.setQuantity(item.getQuantity());
            stockInRequest.setOperatorId(request.getOperatorId());
            stockInRequest.setRemark(buildPurchaseStockInRemark(id, request.getRemark()));
            stockService.stockIn(stockInRequest);
        }

        order.setStatus(STATUS_COMPLETED);
        purchaseOrderMapper.update(order);
    }

    @Override
    public Object getPurchaseOrderDetail(Long id) {
        return getExistingOrder(id);
    }

    @Override
    public Object listPurchaseOrders() {
        return purchaseOrderMapper.findAll();
    }

    private PurchaseOrder getExistingOrder(Long id) {
        if (id == null) {
            throw new BusinessException("Purchase order id is required.");
        }

        PurchaseOrder order = purchaseOrderMapper.findById(id);
        if (order == null) {
            throw new BusinessException("Purchase order does not exist.");
        }
        return order;
    }

    private void ensureStatus(PurchaseOrder order, String expectedStatus) {
        if (!expectedStatus.equals(order.getStatus())) {
            throw new BusinessException("Invalid purchase order status.");
        }
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

    private void validateApproveRequest(PurchaseApproveRequest request) {
        if (request == null || request.getApproverId() == null) {
            throw new BusinessException("Approver id is required.");
        }
    }

    private void validateCompleteRequest(PurchaseCompleteRequest request) {
        if (request == null || request.getOperatorId() == null) {
            throw new BusinessException("Operator id is required.");
        }
    }

    private String buildPurchaseStockInRemark(Long orderId, String remark) {
        String prefix = "Purchase order stock-in, orderId=" + orderId;
        if (remark == null || remark.trim().isEmpty()) {
            return prefix;
        }
        return prefix + ", " + remark.trim();
    }
}
