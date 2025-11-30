package com.jenkinstutorial.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping("/")
    public String home() {
        return "Store backend is running. Try /api/products";
    }

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

}
