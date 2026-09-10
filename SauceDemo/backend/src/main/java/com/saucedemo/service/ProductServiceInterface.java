package com.saucedemo.service;

import com.saucedemo.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductServiceInterface {

    List<Product> getAllProducts();

    Optional<Product> getProductById(Long id);
}
