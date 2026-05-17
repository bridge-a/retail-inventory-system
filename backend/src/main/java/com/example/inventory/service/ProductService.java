package com.example.inventory.service;

import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductUpdateRequest;

public interface ProductService {
    Long createProduct(ProductCreateRequest request);

    void updateProduct(Long id, ProductUpdateRequest request);

    void disableProduct(Long id);

    Object getProductDetail(Long id);

    Object listProducts();
}
