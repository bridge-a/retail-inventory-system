package com.example.inventory.mapper;

import com.example.inventory.entity.Product;
import java.util.List;

public interface ProductMapper {
    Product findById(Long id);

    Product findByCode(String code);

    void insert(Product product);

    void update(Product product);

    List<Product> findAll();
}