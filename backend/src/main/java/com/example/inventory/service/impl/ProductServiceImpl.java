package com.example.inventory.service.impl;

import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.service.ProductService;

public class ProductServiceImpl implements ProductService {
    @Override
    public Long createProduct(ProductCreateRequest request) {
        // TODO: 校验商品编码唯一，创建商品时 currentStock 默认为 0。
        return null;
    }

    @Override
    public void updateProduct(Long id, ProductUpdateRequest request) {
        // TODO: 不允许通过商品修改接口直接修改 code 和 currentStock。
    }

    @Override
    public void disableProduct(Long id) {
        // TODO: 商品不做物理删除，后续通过 status 停用，避免破坏库存流水关联。
    }

    @Override
    public Object getProductDetail(Long id) {
        return null;
    }

    @Override
    public Object listProducts() {
        return null;
    }
}
