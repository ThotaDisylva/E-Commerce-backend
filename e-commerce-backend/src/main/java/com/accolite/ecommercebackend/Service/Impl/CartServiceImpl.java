package com.accolite.ecommercebackend.Service.Impl;

import com.accolite.ecommercebackend.Entity.Cart;
import com.accolite.ecommercebackend.Entity.Product;
import com.accolite.ecommercebackend.Entity.User;
import com.accolite.ecommercebackend.Exception.ProductQuantityExceededException;
import com.accolite.ecommercebackend.Repository.CartRepository;
import com.accolite.ecommercebackend.Repository.ProductRepository;
import com.accolite.ecommercebackend.Repository.UserRepository;
import com.accolite.ecommercebackend.Service.CartService;
import com.accolite.ecommercebackend.dto.Request.CartItemsQuantity;
import com.accolite.ecommercebackend.dto.Request.CartItemsQuantityRequest;
import com.accolite.ecommercebackend.dto.Response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public String addCartItem(UUID productId) {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByEmailAndDeletedDateIsNull(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));

        Cart cart = cartRepository.findByUserAndProduct(user, product);
        if (cart != null) {
            if (cart.getQuantity() + 1 > product.getQuantityAvailable()) {
                throw new ProductQuantityExceededException("Quantity Exceeds Stock Limit");
            }
            cart.setQuantity(cart.getQuantity() + 1);
        } else {
            if (1 > product.getQuantityAvailable()) {
                throw new ProductQuantityExceededException("Quantity Exceeds Stock Limit");
            }
            cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(1);
        }

        cartRepository.save(cart);

        return "Product added to cart successfully";
    }

    @Override
    public void reduceCartItemQuantity(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid cart ID"));

        if (cart.getQuantity() > 1) {
            cart.setQuantity(cart.getQuantity() - 1);
            cartRepository.save(cart);
        } else {
            throw new IllegalArgumentException("Quantity must be greater than 1 to reduce");
        }
    }

    @Override
    public CartItemRemovedResponse removeCartItem(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid cart ID"));

        Integer quantityBeforeRemoval = cart.getQuantity();
        cartRepository.deleteItemById(cartId);


        return new CartItemRemovedResponse(quantityBeforeRemoval);
    }

    @Override
    public CartPageResponse getCartItems() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByEmailAndDeletedDateIsNull(email);

        List<Cart> cartItems = cartRepository.findByUser(user);

        List<CartPageResponse.CartItemInfoResponse> cartItemInfoResponses = cartItems.stream()
                .map(cart -> new CartPageResponse.CartItemInfoResponse(
                        cart.getCartId(),
                        cart.getProduct().getProductId(),
                        cart.getProduct().getImageUrl(),
                        cart.getProduct().getTitle(),
                        cart.getProduct().getSubtitle(),
                        cart.getProduct().getBrand(),
                        cart.getProduct().getPrice(),
                        cart.getProduct().getDiscountPercent(),
                        cart.getProduct().getDeliveryCharges(),
                        cart.getQuantity()
                ))
                .collect(Collectors.toList());
        return new CartPageResponse(cartItemInfoResponses);
    }

    @Override
    public CartItemUpdateResponse getCartItemsCount() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        User user = userRepository.findByEmailAndDeletedDateIsNull(email);

        Integer cartItemCount = cartRepository.sumQuantityByUser(user);

        if (cartItemCount == null) cartItemCount = 0;
        return new CartItemUpdateResponse(cartItemCount);

    }

    @Override
    public void removeAllCartItem() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByEmailAndDeletedDateIsNull(email);

        cartRepository.deleteAllItemsByUserId(user.getUserId());
    }

    @Override
    public CartItemsQuantityRequest checkCartQuantity(CartItemsQuantityRequest cartItemsQuantityRequest) {

        System.out.println(cartItemsQuantityRequest);
        CartItemsQuantityRequest responseList = new CartItemsQuantityRequest();

        responseList.setCartItemsQuantityDetails(new ArrayList<>());

        cartItemsQuantityRequest.getCartItemsQuantityDetails().forEach(item -> {
            UUID productId = item.getProductId();
            Integer requestedQuantity = item.getQuantity();

            if (requestedQuantity != null) { // Check if requestedQuantity is not null
                Integer availableQuantity = productRepository.findById(productId)
                        .map(product -> product.getQuantityAvailable())
                        .orElse(0);

                Integer responseQuantity = Math.min(requestedQuantity, availableQuantity);
                System.out.println(responseQuantity);
                responseList.getCartItemsQuantityDetails().add(new CartItemsQuantity(productId, responseQuantity));
            } else {
                // Handle the case when requestedQuantity is null
                // You can log a warning or handle it according to your use case
                System.out.println("Requested quantity is null for product ID: " + productId);
            }
        });

        return responseList;
    }
}



/*
1. count cart items
2. list of cartItems product info object
    2.1 productI
    2.2 quantity
    2.3 discounted price
    2.4 delivery charges
    2.5 total amount
3. role


onHomePageLoad
1.categories, subCategories
2. min 10 products from 5-7 subcategoies
3. cartItems count

onCartPageLoad
1.productinfo
    2.1 productI
    2.2 quantity
    2.3 actual price
    2.4 discount percentage
    2.4 delivery charges
    2.5 title
    2.6 subtitle
    2.7 brand
    2.9 image





*/