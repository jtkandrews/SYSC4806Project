package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CheckoutController checkoutController;

    private Book createBook(String isbn, String title, int inventory) {
        return new Book(
                isbn,
                title,
                "Some Author",
                "Some Publisher",
                "Some Genre",
                "Some Description",
                10.0,
                inventory,
                "http://example.com/image.jpg"
        );
    }

    @Test
    void checkout_success_singleItem_updatesInventoryAndSaves() {
        // given
        String isbn = "123";
        Book book = createBook(isbn, "Test Book", 10);

        CheckoutController.CheckoutItem item =
                new CheckoutController.CheckoutItem(isbn, 3);
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(List.of(item));

        when(bookRepository.findAllById(Set.of(isbn)))
                .thenReturn(List.of(book));

        when(bookRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Book> result = checkoutController.checkout(request);

        // then
        assertEquals(1, result.size());
        Book updated = result.get(0);
        assertEquals(7, updated.getInventory(), "Inventory should be decremented by requested quantity");

        verify(bookRepository).findAllById(Set.of(isbn));

        ArgumentCaptor<List<Book>> captor = ArgumentCaptor.forClass(List.class);
        verify(bookRepository).saveAll(captor.capture());
        List<Book> savedBooks = captor.getValue();
        assertEquals(1, savedBooks.size());
        assertEquals(7, savedBooks.get(0).getInventory());
    }

    @Test
    void checkout_success_aggregatesDuplicateIsbns() {
        String isbn = "123";
        Book book = createBook(isbn, "Aggregated Book", 10);

        CheckoutController.CheckoutItem item1 =
                new CheckoutController.CheckoutItem(isbn, 2);
        CheckoutController.CheckoutItem item2 =
                new CheckoutController.CheckoutItem(isbn, 3);
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(List.of(item1, item2));

        when(bookRepository.findAllById(Set.of(isbn)))
                .thenReturn(List.of(book));
        when(bookRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Book> result = checkoutController.checkout(request);

        // then total requested = 5
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getInventory());
    }

    @Test
    void checkout_throws_whenCartIsEmpty() {
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(List.of());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Cart cannot be empty", ex.getReason());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void checkout_throws_whenCartItemsNull() {
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Cart cannot be empty", ex.getReason());
        verifyNoInteractions(bookRepository);
    }


    @Test
    void checkout_throws_whenItemIsNull() {
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(List.of((CheckoutController.CheckoutItem) null));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Each cart item must include an ISBN", ex.getReason());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void checkout_throws_whenIsbnIsNull() {
        CheckoutController.CheckoutItem item =
                new CheckoutController.CheckoutItem(null, 1);
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(List.of(item));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Each cart item must include an ISBN", ex.getReason());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void checkout_throws_whenIsbnBlank() {
        CheckoutController.CheckoutItem item =
                new CheckoutController.CheckoutItem("   ", 1);
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(List.of(item));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Each cart item must include an ISBN", ex.getReason());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void checkout_throws_whenQuantityZeroOrNegative() {
        CheckoutController.CheckoutItem itemZero =
                new CheckoutController.CheckoutItem("123", 0);
        CheckoutController.CheckoutRequest requestZero =
                new CheckoutController.CheckoutRequest(List.of(itemZero));

        ResponseStatusException exZero = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(requestZero)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exZero.getStatusCode());
        assertEquals("Quantity must be at least 1", exZero.getReason());
        verifyNoInteractions(bookRepository);

        CheckoutController.CheckoutItem itemNegative =
                new CheckoutController.CheckoutItem("123", -5);
        CheckoutController.CheckoutRequest requestNegative =
                new CheckoutController.CheckoutRequest(List.of(itemNegative));

        ResponseStatusException exNegative = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(requestNegative)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exNegative.getStatusCode());
        assertEquals("Quantity must be at least 1", exNegative.getReason());
        verifyNoInteractions(bookRepository);
    }


    @Test
    void checkout_throws_notFound_whenSomeBooksMissing() {
        CheckoutController.CheckoutItem item1 =
                new CheckoutController.CheckoutItem("111", 1);
        CheckoutController.CheckoutItem item2 =
                new CheckoutController.CheckoutItem("222", 1);
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(List.of(item1, item2));

        Book existing = createBook("111", "Existing", 5);
        when(bookRepository.findAllById(Set.of("111", "222")))
                .thenReturn(List.of(existing));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Book not found: 222", ex.getReason());
        verify(bookRepository, never()).saveAll(anyList());
    }

    @Test
    void checkout_throws_whenRequestedMoreThanInventory() {
        String isbn = "123";
        Book book = createBook(isbn, "Low Stock Book", 2); // only 2 left

        CheckoutController.CheckoutItem item =
                new CheckoutController.CheckoutItem(isbn, 5); // request 5
        CheckoutController.CheckoutRequest request =
                new CheckoutController.CheckoutRequest(List.of(item));

        when(bookRepository.findAllById(Set.of(isbn)))
                .thenReturn(List.of(book));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> checkoutController.checkout(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Only 2 copies of \"Low Stock Book\" remain.", ex.getReason());
        verify(bookRepository, never()).saveAll(anyList());
    }
}
