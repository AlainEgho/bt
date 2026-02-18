package com.example.backend.repository;

import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart_IdOrderByIdAsc(String cartId);

    void deleteByCart_Id(String cartId);

    /** Distinct carts that contain at least one item owned by the given item owner (includes cart user and dates). */
    @Query("SELECT DISTINCT ci.cart FROM CartItem ci WHERE ci.item.user.id = :itemOwnerId")
    List<Cart> findDistinctCartsByItemOwnerId(@Param("itemOwnerId") Long itemOwnerId);
}
