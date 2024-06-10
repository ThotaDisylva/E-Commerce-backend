package com.accolite.ecommercebackend.Service;

import com.accolite.ecommercebackend.dto.Request.CartItemsQuantityRequest;
import com.accolite.ecommercebackend.dto.Response.CartItemRemovedResponse;
import com.accolite.ecommercebackend.dto.Response.CartItemUpdateResponse;
import com.accolite.ecommercebackend.dto.Response.CartItemsQuantityResponse;
import com.accolite.ecommercebackend.dto.Response.CartPageResponse;

import java.util.List;
import java.util.UUID;

public interface CartService {
    String addCartItem(UUID productId);

    void reduceCartItemQuantity(UUID cartId);

    CartItemRemovedResponse removeCartItem(UUID cartId);

    CartPageResponse getCartItems();

    CartItemUpdateResponse getCartItemsCount();

    void removeAllCartItem();

    CartItemsQuantityRequest checkCartQuantity(CartItemsQuantityRequest cartItemsQuantityRequest);
}
