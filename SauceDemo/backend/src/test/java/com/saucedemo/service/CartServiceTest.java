package com.saucedemo.service;

import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.repository.CartItemRepository;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("P001", "Sauce Labs Backpack", "Mochila", 29.99, "img1.jpg");
        product.setId(1L);
    }

    @Test
    @DisplayName("getCart debe retornar los items de la sesión")
    void getCart_deveRetornarItemsDeLaSesion() {
        CartItem item = new CartItem("session-1", product, 1);
        item.setId(11L);
        when(cartItemRepository.findBySessionId("session-1")).thenReturn(List.of(item));

        List<CartItem> resultado = cartService.getCart("session-1");

        assertThat(resultado).hasSize(1).containsExactly(item);
        verify(cartItemRepository).findBySessionId("session-1");
    }

    @Test
    @DisplayName("addToCart debe crear un nuevo item cuando el producto existe y no hay item previo")
    void addToCart_creaNuevoItemCuandoProductoExisteYNoHayItemPrevio() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findBySessionIdAndProductId("session-1", 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem resultado = cartService.addToCart("session-1", 1L, 3);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getSessionId()).isEqualTo("session-1");
        assertThat(resultado.getProduct()).isEqualTo(product);
        assertThat(resultado.getQuantity()).isEqualTo(3);

        verify(productRepository).findById(1L);
        verify(cartItemRepository).findBySessionIdAndProductId("session-1", 1L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("updateQuantity debe actualizar la cantidad cuando el item existe")
    void updateQuantity_actualizaCantidadCuandoItemExiste() {
        CartItem existing = new CartItem("session-1", product, 1);
        existing.setId(10L);
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem resultado = cartService.updateQuantity(10L, 7);

        assertThat(resultado.getQuantity()).isEqualTo(7);
        verify(cartItemRepository).findById(10L);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("removeItem debe llamar a deleteById del repositorio")
    void removeItem_llamaDeleteByIdDelRepositorio() {
        cartService.removeItem(1L);

        verify(cartItemRepository).deleteById(1L);
    }

    @Test
    @DisplayName("getCart debe retornar lista vacía cuando la sesión no tiene items")
    void getCart_debeRetornarListaVaciaCuandoSesionNoTieneItems() {
        when(cartItemRepository.findBySessionId("session-X")).thenReturn(Collections.emptyList());

        List<CartItem> resultado = cartService.getCart("session-X");

        assertThat(resultado).isEmpty();
        verify(cartItemRepository).findBySessionId("session-X");
    }

    @Test
    @DisplayName("addToCart debe incrementar la cantidad cuando el item ya existe en el carrito")
    void addToCart_incrementaCantidadCuandoItemYaExisteEnCarrito() {
        CartItem existing = new CartItem("session-1", product, 2);
        existing.setId(10L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findBySessionIdAndProductId("session-1", 1L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem resultado = cartService.addToCart("session-1", 1L, 3);

        assertThat(resultado.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(existing);
    }

    @Test
    @DisplayName("addToCart debe lanzar NoSuchElementException cuando el producto no existe")
    void addToCart_lanzaExcepcionCuandoProductoNoExiste() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart("session-1", 99L, 1))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("updateQuantity debe lanzar NoSuchElementException cuando el item no existe")
    void updateQuantity_lanzaExcepcionCuandoItemNoExiste() {
        when(cartItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateQuantity(99L, 5))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("removeItem debe propagar la excepción si el repositorio falla")
    void removeItem_propagaExcepcionSiRepositorioFalla() {
        doThrow(new RuntimeException("Error en BD")).when(cartItemRepository).deleteById(99L);

        assertThatThrownBy(() -> cartService.removeItem(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error en BD");
    }
}
