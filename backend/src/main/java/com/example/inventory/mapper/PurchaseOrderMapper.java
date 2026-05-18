package com.example.inventory.mapper;

import com.example.inventory.entity.PurchaseOrder;
import java.util.List;

public interface PurchaseOrderMapper {
    void insert(PurchaseOrder order);

    PurchaseOrder findById(Long id);

    void update(PurchaseOrder order);

    List<PurchaseOrder> findAll();
}
