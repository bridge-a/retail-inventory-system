package com.example.inventory.mapper;

import com.example.inventory.entity.PurchaseOrderItem;
import java.util.List;

public interface PurchaseOrderItemMapper {
    void insert(PurchaseOrderItem item);

    List<PurchaseOrderItem> findByOrderId(Long orderId);
}
