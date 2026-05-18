package com.example.inventory.service.impl;

import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.service.ProductService;

public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Long createProduct(ProductCreateRequest request) {
        validateCreateRequest(request);

        Product existing = productMapper.findByCode(request.getCode());
        if (existing != null) {
            throw new BusinessException("Product code already exists.");
        }

        Product product = new Product();
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName().trim());
        product.setCode(request.getCode().trim());
        product.setUnit(request.getUnit().trim());
        product.setCurrentStock(0);
        product.setSafeStock(defaultSafeStock(request.getSafeStock()));
        product.setStatus(1);

        productMapper.insert(product);
        return product.getId();
    }

    @Override
    public void updateProduct(Long id, ProductUpdateRequest request) {
        if (request == null) {
            throw new BusinessException("Product update request is required.");
        }
        validateProductId(id);
        validateSafeStock(request.getSafeStock());
        validateStatus(request.getStatus());

        Product product = getExistingProduct(id);
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (!isBlank(request.getName())) {
            product.setName(request.getName().trim());
        }
        if (!isBlank(request.getUnit())) {
            product.setUnit(request.getUnit().trim());
        }
        if (request.getSafeStock() != null) {
            product.setSafeStock(request.getSafeStock());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        productMapper.update(product);
    }

    @Override
    public void disableProduct(Long id) {
        validateProductId(id);

        Product product = getExistingProduct(id);
        product.setStatus(0);
        productMapper.update(product);
    }

    @Override
    public Object getProductDetail(Long id) {
        validateProductId(id);
        return getExistingProduct(id);
    }

    @Override
    public Object listProducts() {
        return productMapper.findAll();
    }

    private Product getExistingProduct(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException("Product does not exist.");
        }
        return product;
    }

    private void validateCreateRequest(ProductCreateRequest request) {
        if (request == null) {
            throw new BusinessException("Product create request is required.");
        }
        if (request.getCategoryId() == null) {
            throw new BusinessException("Product category is required.");
        }
        if (isBlank(request.getName())) {
            throw new BusinessException("Product name is required.");
        }
        if (isBlank(request.getCode())) {
            throw new BusinessException("Product code is required.");
        }
        if (isBlank(request.getUnit())) {
            throw new BusinessException("Product unit is required.");
        }
        validateSafeStock(request.getSafeStock());
    }

    private void validateProductId(Long id) {
        if (id == null) {
            throw new BusinessException("Product id is required.");
        }
    }

    private void validateSafeStock(Integer safeStock) {
        if (safeStock != null && safeStock < 0) {
            throw new BusinessException("Safe stock cannot be negative.");
        }
    }

    private void validateStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException("Product status must be 0 or 1.");
        }
    }

    private int defaultSafeStock(Integer safeStock) {
        return safeStock == null ? 0 : safeStock;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
