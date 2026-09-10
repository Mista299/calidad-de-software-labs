package com.saucedemo.service;

import com.saucedemo.model.Product;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        productA = new Product("P001", "Sauce Labs Backpack", "Mochila", 29.99, "img1.jpg");
        productA.setId(1L);

        productB = new Product("P002", "Sauce Labs Bike Light", "Luz", 9.99, "img2.jpg");
        productB.setId(2L);
    }

    @Test
    @DisplayName("getAllProducts debe retornar la lista del repositorio")
    void getAllProducts_debeRetornarProductosDelRepositorio() {
        when(productRepository.findAll()).thenReturn(List.of(productA, productB));

        List<Product> resultado = productService.getAllProducts();

        assertThat(resultado).hasSize(2).containsExactly(productA, productB);
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("getProductById debe retornar el producto envuelto en Optional cuando existe")
    void getProductById_debeRetornarProductoCuandoExiste() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(productA));

        Optional<Product> resultado = productService.getProductById(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado).contains(productA);
        verify(productRepository).findById(1L);
    }
}
