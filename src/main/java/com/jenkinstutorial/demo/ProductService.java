package com.jenkinstutorial.demo;

import java.util.List;
import com.jenkinstutorial.demo.Product;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product createProduct(Product product);
    void deleteProduct(Long id);
}
