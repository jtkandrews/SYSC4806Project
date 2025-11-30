package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Order;
import com.amazin.svelteamazin.model.User;
import com.amazin.svelteamazin.repository.OrderRepository;
import com.amazin.svelteamazin.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public record OrderItemResponse(String isbn, String title, double price, int quantity, String imageUrl) {}

    public record OrderResponse(Long id, String createdAt, List<OrderItemResponse> items) {}

    @GetMapping
    public List<OrderResponse> getOrders(HttpServletRequest request) {
        User user = requireUser(request);
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getCreatedAt().toString(),
                        order.getItems().stream()
                                .map(oi -> new OrderItemResponse(oi.getIsbn(), oi.getTitle(), oi.getPrice(), oi.getQuantity(), oi.getImageUrl()))
                                .toList()
                ))
                .toList();
    }

    private User requireUser(HttpServletRequest request) {
        String username = null;
        String role = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("username".equals(cookie.getName())) {
                    username = cookie.getValue();
                }
                if ("role".equals(cookie.getName())) {
                    role = cookie.getValue();
                }
            }
        }
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        if (!"USER".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only regular users can access orders");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        return user;
    }
}