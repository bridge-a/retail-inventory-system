package com.example.inventory.service.impl;

import com.example.inventory.dto.StockInRequest;
import com.example.inventory.dto.StockOutRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.StockRecord;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.mapper.StockRecordMapper;
import com.example.inventory.service.StockService;

import java.time.LocalDateTime;

public class StockServiceImpl implements StockService {
    private static final String CHANGE_TYPE_IN = "IN";
    private static final String CHANGE_TYPE_OUT = "OUT";
    private static final String SOURCE_TYPE_MANUAL = "MANUAL";

    private final ProductMapper productMapper;
    private final StockRecordMapper stockRecordMapper;

    public StockServiceImpl(ProductMapper productMapper, StockRecordMapper stockRecordMapper) {
        this.productMapper = productMapper;
        this.stockRecordMapper = stockRecordMapper;
    }

    @Override
    public void stockIn(StockInRequest request) {
        validateStockInRequest(request);

        Product product = getExistingProduct(request.getProductId());
        int beforeStock = safeStock(product.getCurrentStock());
        int afterStock = beforeStock + request.getQuantity();

        product.setCurrentStock(afterStock);
        productMapper.update(product);

        StockRecord record = buildStockRecord(
                product.getId(),
                CHANGE_TYPE_IN,
                request.getQuantity(),
                beforeStock,
                afterStock,
                request.getOperatorId(),
                request.getRemark()
        );
        stockRecordMapper.insert(record);
    }

    @Override
    public void stockOut(StockOutRequest request) {
        validateStockOutRequest(request);

        Product product = getExistingProduct(request.getProductId());
        int beforeStock = safeStock(product.getCurrentStock());
        if (request.getQuantity() > beforeStock) {
            throw new BusinessException("Insufficient stock.");
        }

        int afterStock = beforeStock - request.getQuantity();
        product.setCurrentStock(afterStock);
        productMapper.update(product);

        StockRecord record = buildStockRecord(
                product.getId(),
                CHANGE_TYPE_OUT,
                request.getQuantity(),
                beforeStock,
                afterStock,
                request.getOperatorId(),
                request.getRemark()
        );
        stockRecordMapper.insert(record);
    }

    @Override
    public Object listStockRecords() {
        return stockRecordMapper.findAll();
    }

    @Override
    public Object listStockRecordsByProduct(Long productId) {
        if (productId == null) {
            throw new BusinessException("Product id is required.");
        }
        return stockRecordMapper.findByProductId(productId);
    }

    private Product getExistingProduct(Long productId) {
        if (productId == null) {
            throw new BusinessException("Product id is required.");
        }

        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException("Product does not exist.");
        }
        return product;
    }

    private void validateStockInRequest(StockInRequest request) {
        if (request == null) {
            throw new BusinessException("Stock-in request is required.");
        }
        validateQuantity(request.getQuantity());
        validateOperator(request.getOperatorId());
    }

    private void validateStockOutRequest(StockOutRequest request) {
        if (request == null) {
            throw new BusinessException("Stock-out request is required.");
        }
        validateQuantity(request.getQuantity());
        validateOperator(request.getOperatorId());
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("Stock quantity must be greater than 0.");
        }
    }

    private void validateOperator(Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException("Operator id is required.");
        }
    }

    private int safeStock(Integer stock) {
        return stock == null ? 0 : stock;
    }

    private StockRecord buildStockRecord(
            Long productId,
            String changeType,
            Integer quantity,
            Integer beforeStock,
            Integer afterStock,
            Long operatorId,
            String remark
    ) {
        StockRecord record = new StockRecord();
        record.setProductId(productId);
        record.setChangeType(changeType);
        record.setQuantity(quantity);
        record.setBeforeStock(beforeStock);
        record.setAfterStock(afterStock);
        record.setSourceType(SOURCE_TYPE_MANUAL);
        record.setOperatorId(operatorId);
        record.setRemark(remark);
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }
}
