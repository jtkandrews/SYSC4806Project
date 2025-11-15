package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/books")
public class OwnerBookController {

    private final BookRepository repo;

    public OwnerBookController(BookRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Book book) {
        // TODO: validation (isbn required, etc.)
        return repo.save(book);
    }

    @PutMapping("/{isbn}")
    public Book update(@PathVariable String isbn, @RequestBody Book book) {
        book.setIsbn(isbn);
        return repo.save(book);
    }

    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String isbn) {
        repo.deleteById(isbn);
    }
}
