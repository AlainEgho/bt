package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PaymentService paymentService;

    @Transactional(readOnly = true)
    public List<CartResponse> findAllByUserId(Long userId) {
        return cartRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(CartResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CartResponse findById(String id, Long userId) {
        Cart cart = cartRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse create(CreateCartRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Cart cart = new Cart();
        cart.setId(UUID.randomUUID().toString());
        cart.setUser(user);
        cart.setStatus(request.getStatus() != null ? request.getStatus() : CartStatus.PENDING);
        cart.setPaymentMethod(request.getPaymentMethod());
        cart.setEventDate(request.getEventDate());
        cart = cartRepository.save(cart);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (CartItemDto itemDto : request.getItems()) {
                Item item = itemRepository.findById(itemDto.getItemId())
                        .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemDto.getItemId()));
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setItem(item);
                cartItem.setQuantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : 1);
                cartItemRepository.save(cartItem);
            }
        }

        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse update(String id, UpdateCartRequest request, Long userId) {
        Cart cart = cartRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        if (request.getStatus() != null) {
            cart.setStatus(request.getStatus());
        }
        if (request.getPaymentMethod() != null) {
            cart.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getEventDate() != null) {
            cart.setEventDate(request.getEventDate());
        }

        if (request.getItems() != null) {
            cartItemRepository.deleteByCart_Id(cart.getId());
            cart.getCartItems().clear();
            for (CartItemDto itemDto : request.getItems()) {
                Item item = itemRepository.findById(itemDto.getItemId())
                        .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemDto.getItemId()));
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setItem(item);
                cartItem.setQuantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : 1);
                cart.getCartItems().add(cartItem);
            }
            cartRepository.save(cart);
        }

        cart = cartRepository.findById(cart.getId()).orElse(cart);
        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public void delete(String id, Long userId) {
        Cart cart = cartRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        cartRepository.delete(cart);
    }

    @Transactional(readOnly = true)
    public List<CartResponse> findByUserIdAndStatus(Long userId, CartStatus status) {
        return cartRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, status).stream()
                .map(CartResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal getCartTotal(String id, Long userId) {
        Cart cart = cartRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        return paymentService.computeCartTotal(cart);
    }
}
