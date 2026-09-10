package com.saucedemo.controller;

import com.saucedemo.model.Product;
import com.saucedemo.service.ProductServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductServiceInterface productService;

    @InjectMocks
    private ProductController productController;

    private Product productA;

    @BeforeEach
    void setUp() {
        productA = new Product("P001", "Sauce Labs Backpack", "Mochila", 29.99, "img1.jpg");
        productA.setId(1L);
    }

    @Test
    @DisplayName("getAllProducts debe retornar la lista del service")
    void getAllProducts_retornaListaDelService() {
        Product productB = new Product("P002", "Sauce Labs Bike Light", "Luz", 9.99, "img2.jpg");
        productB.setId(2L);
        when(productService.getAllProducts()).thenReturn(List.of(productA, productB));

        List<Product> resultado = productController.getAllProducts();

        assertThat(resultado).hasSize(2).containsExactly(productA, productB);
        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("getProductById debe retornar 200 con el producto cuando existe")
    void getProductById_retorna200ConProductoCuandoExiste() {
        when(productService.getProductById(1L)).thenReturn(Optional.of(productA));

        ResponseEntity<Product> respuesta = productController.getProductById(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(productA);
        verify(productService).getProductById(1L);
    }
}
