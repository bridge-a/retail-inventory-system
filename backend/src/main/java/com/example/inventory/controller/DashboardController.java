package com.example.inventory.controller;

import com.example.inventory.entity.Product;
import com.example.inventory.entity.PurchaseOrder;
import com.example.inventory.entity.PurchaseOrderItem;
import com.example.inventory.entity.StockRecord;
import com.example.inventory.entity.StockWarning;
import com.example.inventory.entity.User;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.mapper.PurchaseOrderItemMapper;
import com.example.inventory.mapper.PurchaseOrderMapper;
import com.example.inventory.mapper.StockRecordMapper;
import com.example.inventory.mapper.StockWarningMapper;
import com.example.inventory.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ProductMapper productMapper;
    private final StockRecordMapper stockRecordMapper;
    private final StockWarningMapper stockWarningMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final UserMapper userMapper;

    public DashboardController(
            ProductMapper productMapper,
            StockRecordMapper stockRecordMapper,
            StockWarningMapper stockWarningMapper,
            PurchaseOrderMapper purchaseOrderMapper,
            PurchaseOrderItemMapper purchaseOrderItemMapper,
            UserMapper userMapper
    ) {
        this.productMapper = productMapper;
        this.stockRecordMapper = stockRecordMapper;
        this.stockWarningMapper = stockWarningMapper;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderItemMapper = purchaseOrderItemMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("connected", true);
        dashboard.put("products", productMapper.findAll().stream().map(this::productView).toList());
        dashboard.put("records", stockRecordMapper.findAll().stream().map(this::stockRecordView).toList());
        dashboard.put("warnings", stockWarningMapper.findAll().stream().map(this::warningView).toList());
        dashboard.put("purchaseOrders", purchaseOrderMapper.findAll().stream().map(this::purchaseOrderView).toList());
        return dashboard;
    }

    private Map<String, Object> productView(Product product) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", product.getId());
        view.put("code", product.getCode());
        view.put("name", product.getName());
        view.put("category", categoryName(product.getCategoryId()));
        view.put("categoryId", product.getCategoryId());
        view.put("unit", product.getUnit());
        view.put("currentStock", product.getCurrentStock());
        view.put("safeStock", product.getSafeStock());
        view.put("status", product.getStatus());
        return view;
    }

    private Map<String, Object> stockRecordView(StockRecord record) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", record.getId());
        view.put("time", record.getCreatedAt() == null ? "" : record.getCreatedAt().format(TIME_FORMATTER));
        view.put("productId", record.getProductId());
        view.put("type", record.getChangeType());
        view.put("quantity", record.getQuantity());
        view.put("beforeStock", record.getBeforeStock());
        view.put("afterStock", record.getAfterStock());
        view.put("source", record.getSourceType());
        view.put("operator", userName(record.getOperatorId()));
        return view;
    }

    private Map<String, Object> warningView(StockWarning warning) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", warning.getId());
        view.put("productId", warning.getProductId());
        view.put("warningStock", warning.getWarningStock());
        view.put("safeStock", warning.getSafeStock());
        view.put("status", warning.getStatus());
        return view;
    }

    private Map<String, Object> purchaseOrderView(PurchaseOrder order) {
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.findByOrderId(order.getId());
        PurchaseOrderItem firstItem = items.isEmpty() ? null : items.get(0);

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", order.getId());
        view.put("code", "PO-20260521-" + String.format("%03d", order.getId()));
        view.put("productId", firstItem == null ? null : firstItem.getProductId());
        view.put("quantity", firstItem == null ? 0 : firstItem.getQuantity());
        view.put("applicant", userName(order.getApplicantId()));
        view.put("applicantRole", userRole(order.getApplicantId()));
        view.put("reason", order.getReason());
        view.put("status", order.getStatus());
        view.put("approver", order.getApproverId() == null ? "" : userName(order.getApproverId()));
        view.put("remark", order.getApprovalRemark());
        return view;
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null) {
            return "未分类";
        }
        if (categoryId == 1L) {
            return "饮品";
        }
        if (categoryId == 2L) {
            return "日用品";
        }
        if (categoryId == 3L) {
            return "出行";
        }
        return "其他";
    }

    private String userName(Long userId) {
        if (userId == null) {
            return "";
        }
        User user = userMapper.findById(userId);
        return user == null ? "" : user.getRealName();
    }

    private String userRole(Long userId) {
        if (userId == null) {
            return "";
        }
        User user = userMapper.findById(userId);
        return user == null ? "" : user.getRole();
    }
}
