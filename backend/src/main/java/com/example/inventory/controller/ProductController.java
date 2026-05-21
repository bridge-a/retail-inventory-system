package com.example.inventory.controller;

import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.service.ProductService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Long createProduct(@RequestBody ProductCreateRequest request) {
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    public void updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public void disableProduct(@PathVariable Long id) {
        productService.disableProduct(id);
    }

    @GetMapping("/{id}")
    public Object getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    @GetMapping
    public Object listProducts() {
        return productService.listProducts();
    }
}
