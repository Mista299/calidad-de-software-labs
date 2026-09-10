package com.saucedemo.service;

import com.saucedemo.model.CartItem;

import java.util.List;

public interface CartServiceInterface {

    List<CartItem> getCart(String sessionId);

    CartItem addToCart(String sessionId, Long productId, Integer quantity);

    CartItem updateQuantity(Long itemId, Integer quantity);

    void removeItem(Long itemId);
}
