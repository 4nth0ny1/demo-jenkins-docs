package com.jenkinstutorial.demo;

import com.jenkinstutorial.demo.Product;
import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AccountValidationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Set<ConstraintViolation<Product>> validate(Product product) {
        return validator.validate(product);
    }

    @Test
    void shouldPassValidation_WhenDataIsValid() {
        Product prod = new Product();
        prod.setName("Cigarettes");
        prod.setPrice(5.99);

        Set<ConstraintViolation<Product>> violations = validate(prod);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidation_WhenPriceIsEmptyOrMissing() {
        Product prod = new Product();
        prod.setName("Basketball");

        Set<ConstraintViolation<Product>> violations = validate(prod);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("price")));
    }

}
