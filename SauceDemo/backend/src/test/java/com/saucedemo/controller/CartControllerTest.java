package com.saucedemo.controller;

import com.saucedemo.dto.AddToCartRequest;
import com.saucedemo.dto.UpdateCartItemRequest;
import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.service.CartServiceInterface;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartServiceInterface cartService;

    @InjectMocks
    private CartController cartController;

    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        product = new Product("P001", "Sauce Labs Backpack", "Mochila", 29.99, "img1.jpg");
        product.setId(1L);
        cartItem = new CartItem("session-1", product, 2);
        cartItem.setId(10L);
    }

    @Test
    @DisplayName("getCart debe retornar los items del service")
    void getCart_retornaItemsDelService() {
        when(cartService.getCart("session-1")).thenReturn(List.of(cartItem));

        List<CartItem> resultado = cartController.getCart("session-1");

        assertThat(resultado).hasSize(1).containsExactly(cartItem);
        verify(cartService).getCart("session-1");
    }

    @Test
    @DisplayName("addToCart debe retornar 200 con el item cuando el service responde OK")
    void addToCart_retorna200ConItemCuandoExitoso() {
        AddToCartRequest request = new AddToCartRequest();
        request.setSessionId("session-1");
        request.setProductId(1L);
        request.setQuantity(2);

        when(cartService.addToCart("session-1", 1L, 2)).thenReturn(cartItem);

        ResponseEntity<CartItem> respuesta = cartController.addToCart(request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(cartItem);
        verify(cartService).addToCart("session-1", 1L, 2);
    }

    @Test
    @DisplayName("updateQuantity debe retornar 200 con el item cuando el service responde OK")
    void updateQuantity_retorna200ConItemCuandoExitoso() {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartService.updateQuantity(10L, 5)).thenReturn(cartItem);

        ResponseEntity<CartItem> respuesta = cartController.updateQuantity(10L, request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(cartItem);
        verify(cartService).updateQuantity(10L, 5);
    }

    @Test
    @DisplayName("removeItem debe retornar 204 y llamar al service")
    void removeItem_retorna204() {
        ResponseEntity<Void> respuesta = cartController.removeItem(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(cartService).removeItem(1L);
    }
}
