package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
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

    public CheckoutController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public record CheckoutItem(String isbn, int quantity) {}

    public record CheckoutRequest(List<CheckoutItem> items) {}

    @PostMapping("/checkout")
    @Transactional
    public List<Book> checkout(@RequestBody CheckoutRequest request) {
        if (request.items == null || request.items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart cannot be empty");
        }

        Map<String, Integer> aggregated = new HashMap<>();
        for (CheckoutItem item : request.items) {
            if (item == null || item.isbn == null || item.isbn.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each cart item must include an ISBN");
            }
            if (item.quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
            }
            aggregated.merge(item.isbn, item.quantity, Integer::sum);
        }

        Iterable<Book> fetched = bookRepository.findAllById(aggregated.keySet());
        List<Book> books = new ArrayList<>();
        fetched.forEach(books::add);

        if (books.size() != aggregated.size()) {
            Set<String> foundIsbns = books.stream().map(Book::getIsbn).collect(Collectors.toSet());
            String missing = aggregated.keySet().stream()
                    .filter(isbn -> !foundIsbns.contains(isbn))
                    .sorted()
                    .collect(Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + missing);
        }

        for (Book book : books) {
            int requested = aggregated.get(book.getIsbn());
            if (requested > book.getInventory()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Only " + book.getInventory() + " copies of \"" + book.getTitle() + "\" remain."
                );
            }
            book.setInventory(book.getInventory() - requested);
        }

        return bookRepository.saveAll(books);
    }
}