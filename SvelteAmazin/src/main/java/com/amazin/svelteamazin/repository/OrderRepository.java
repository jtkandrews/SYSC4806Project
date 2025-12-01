package com.amazin.svelteamazin.repository;

import com.amazin.svelteamazin.model.Order;
import com.amazin.svelteamazin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}