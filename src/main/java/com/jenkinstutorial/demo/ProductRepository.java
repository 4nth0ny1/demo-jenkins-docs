package com.jenkinstutorial.demo;

import com.jenkinstutorial.demo.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Spring Data JPA repository for Product entities.
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}