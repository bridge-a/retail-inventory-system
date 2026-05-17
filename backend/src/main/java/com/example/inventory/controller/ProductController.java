package com.example.inventory.controller;

import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.service.ProductService;

public class ProductController {
    private ProductService productService;

    public Long createProduct(ProductCreateRequest request) {
        return productService.createProduct(request);
    }

    public void updateProduct(Long id, ProductUpdateRequest request) {
        productService.updateProduct(id, request);
    }

    public void disableProduct(Long id) {
        productService.disableProduct(id);
    }

    public Object getProductDetail(Long id) {
        return productService.getProductDetail(id);
    }

    public Object listProducts() {
        return productService.listProducts();
    }
}
