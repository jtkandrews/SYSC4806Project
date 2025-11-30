package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.CartItem;
import com.amazin.svelteamazin.model.Order;
import com.amazin.svelteamazin.model.OrderItem;
import com.amazin.svelteamazin.model.User;
import com.amazin.svelteamazin.model.BookRepository;
import com.amazin.svelteamazin.repository.CartItemRepository;
import com.amazin.svelteamazin.repository.OrderRepository;
import com.amazin.svelteamazin.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CheckoutController {

    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public CheckoutController(BookRepository bookRepository,
                              CartItemRepository cartItemRepository,
                              OrderRepository orderRepository,
                              UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    //public record CheckoutItem(String isbn, int quantity) {}
    public record CartItemRequest(String isbn, int quantity) {}

    //public record CheckoutRequest(List<CheckoutItem> items) {}
    public record CartItemResponse(String isbn, String title, double price, int quantity, String imageUrl, int inventory) {}

    public record OrderItemResponse(String isbn, String title, double price, int quantity, String imageUrl) {}

    public record OrderResponse(Long id, String createdAt, List<OrderItemResponse> items) {}

    public record CheckoutResponse(OrderResponse order, List<Book> updatedBooks) {}

    @GetMapping
    public List<CartItemResponse> getCart(HttpServletRequest request) {
        User user = requireUser(request);
        List<CartItem> items = cartItemRepository.findByUser(user);
        return toCartResponses(items);
    }

    @PostMapping("/items")
    @Transactional
    public List<CartItemResponse> upsertItem(@RequestBody CartItemRequest request, HttpServletRequest httpRequest) {
        User user = requireUser(httpRequest);
        if (request.isbn == null || request.isbn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN is required");
        }
        if (request.quantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be zero or positive");
        }

        Book book = bookRepository.findById(request.isbn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (request.quantity > book.getInventory()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only " + book.getInventory() + " copies of \"" + book.getTitle() + "\" remain.");
        }

        Optional<CartItem> existing = cartItemRepository.findByUserAndBook(user, book);

        if (request.quantity == 0) {
            existing.ifPresent(cartItemRepository::delete);
        } else {
            CartItem item = existing.orElseGet(() -> new CartItem(user, book, request.quantity));
            item.setQuantity(request.quantity);
            cartItemRepository.save(item);
        }

        return toCartResponses(cartItemRepository.findByUser(user));
    }

    @DeleteMapping("/items/{isbn}")
    @Transactional
    public List<CartItemResponse> removeItem(@PathVariable String isbn, HttpServletRequest request) {
        User user = requireUser(request);
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        cartItemRepository.findByUserAndBook(user, book).ifPresent(cartItemRepository::delete);
        return toCartResponses(cartItemRepository.findByUser(user));
    }

    @PostMapping("/checkout")
    @Transactional
    public CheckoutResponse checkout(HttpServletRequest request) {
        User user = requireUser(request);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart cannot be empty");
        }

        Map<String, CartItem> byIsbn = cartItems.stream()
                .collect(Collectors.toMap(item -> item.getBook().getIsbn(), item -> item));

        List<Book> books = bookRepository.findAllById(byIsbn.keySet());
        if (books.size() != byIsbn.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more books could not be found");
        }

        for (Book book : books) {
            int requested = byIsbn.get(book.getIsbn()).getQuantity();
            if (requested <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantities must be positive");
            }
            if (requested > book.getInventory()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Only " + book.getInventory() + " copies of \"" + book.getTitle() + "\" remain."
                );
            }
            book.setInventory(book.getInventory() - requested);
        }

        List<Book> updatedBooks = bookRepository.saveAll(books);

        Order order = new Order(user);
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            orderItems.add(new OrderItem(order, book.getIsbn(), book.getTitle(), book.getPrice(), cartItem.getQuantity(), book.getImageUrl()));
        }
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteByUser(user);

        OrderResponse orderResponse = new OrderResponse(
                savedOrder.getId(),
                savedOrder.getCreatedAt().toString(),
                savedOrder.getItems().stream()
                        .map(oi -> new OrderItemResponse(oi.getIsbn(), oi.getTitle(), oi.getPrice(), oi.getQuantity(), oi.getImageUrl()))
                        .toList()
        );

        return new CheckoutResponse(orderResponse, updatedBooks);
    }

    private List<CartItemResponse> toCartResponses(List<CartItem> items) {
        return items.stream()
                .map(item -> {
                    Book book = item.getBook();
                    return new CartItemResponse(book.getIsbn(), book.getTitle(), book.getPrice(), item.getQuantity(), book.getImageUrl(), book.getInventory());
                })
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only regular users can access the cart");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        return user;
    }
}