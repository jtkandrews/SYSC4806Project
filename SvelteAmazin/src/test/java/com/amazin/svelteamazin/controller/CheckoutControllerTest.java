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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private CheckoutController checkoutController;

    private User user;

    @BeforeEach
    void setUp() {
        checkoutController = new CheckoutController(bookRepository, cartItemRepository, orderRepository, userRepository);
        user = new User(1L, "alice", "password", "USER");
    }

    @Test
    void checkoutCreatesOrderUpdatesInventoryAndClearsCart() throws Exception {
        Book first = new Book("isbn-1", "First", "a", "p", "g", "d", 10.0, 5, "img1");
        Book second = new Book("isbn-2", "Second", "a", "p", "g", "d", 20.0, 3, "img2");

        CartItem item1 = new CartItem(user, first, 2);
        CartItem item2 = new CartItem(user, second, 1);

        when(userRepository.findByUsername("alice")).thenReturn(user);
        when(cartItemRepository.findByUser(user)).thenReturn(List.of(item1, item2));
        when(bookRepository.findAllById(Map.of("isbn-1", item1, "isbn-2", item2).keySet()))
                .thenReturn(List.of(first, second));
        when(bookRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, 1L);
            // also make sure items point back to order like JPA would
            for (OrderItem item : order.getItems()) {
                Field orderField = OrderItem.class.getDeclaredField("order");
                orderField.setAccessible(true);
                orderField.set(item, order);
            }
            return order;
        });

        CheckoutController.CheckoutResponse response = checkoutController.checkout(requestWithUser());

        assertThat(response.order().id()).isEqualTo(1L);
        assertThat(response.updatedBooks()).extracting(Book::getInventory)
                .containsExactlyInAnyOrder(3, 2);

        assertThat(first.getInventory()).isEqualTo(3);
        assertThat(second.getInventory()).isEqualTo(2);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(cartItemRepository).deleteByUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("alice");
    }

    @Test
    void checkoutFailsWhenCartIsEmpty() {
        when(userRepository.findByUsername("alice")).thenReturn(user);
        when(cartItemRepository.findByUser(user)).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> checkoutController.checkout(requestWithUser()));

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void checkoutRejectsQuantitiesAboveInventory() {
        Book scarce = new Book("limited", "Limited", "a", "p", "g", "d", 9.99, 1, "img");
        CartItem cartItem = new CartItem(user, scarce, 2);

        when(userRepository.findByUsername("alice")).thenReturn(user);
        when(cartItemRepository.findByUser(user)).thenReturn(List.of(cartItem));
        when(bookRepository.findAllById(any())).thenReturn(List.of(scarce));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> checkoutController.checkout(requestWithUser()));

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(scarce.getInventory()).isEqualTo(1);
        verifyNoInteractions(orderRepository);
    }

    private HttpServletRequest requestWithUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("username", "alice"), new Cookie("role", "USER"));
        return request;
    }
}