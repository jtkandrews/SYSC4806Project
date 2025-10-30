package com.amazonbookstore.controller;

import com.amazonbookstore.model.Book;
import com.amazonbookstore.model.BookStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookStore bookStore;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookStore.getAllBooks();
    }

    @GetMapping("/{isbn}")
    public Book getBookByIsbn(@PathVariable String isbn) {
        return bookStore.getBookByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found: " + isbn));
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String query) {
        return bookStore.searchBooks(query);
    }
}
