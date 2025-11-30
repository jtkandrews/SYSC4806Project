package com.amazin.svelteamazin.repository;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.CartItem;
import com.amazin.svelteamazin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndBook(User user, Book book);
    void deleteByUser(User user);
}