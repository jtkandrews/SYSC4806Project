package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookRepository repo;

    public BookController(BookRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/books
     * Return all books.
     */
    @GetMapping
    public List<Book> getAllBooks() {
        return repo.findAll();
    }

    /**
     * GET /api/books/{isbn}
     * Return a single book by ISBN.
     */
    @GetMapping("/{isbn}")
    public Book getBookByIsbn(@PathVariable String isbn) {
        return repo.findById(isbn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + isbn));
    }

    /**
     * GET /api/books/search?query=...
     * Case-insensitive search on title OR author.
     */
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String query) {
        return repo.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    /**
     * POST /api/books
     * Create a new book (or overwrite if ISBN exists).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Book book) {
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN is required");
        }
        return repo.save(book);
    }

    /**
     * PUT /api/books/{isbn}
     * Update an existing book (creates if not present).
     */
    @PutMapping("/{isbn}")
    public Book upsert(@PathVariable String isbn, @RequestBody Book book) {
        // Ensure path ISBN wins
        book.setIsbn(isbn);
        return repo.save(book);
    }

    /**
     * DELETE /api/books/{isbn}
     * Delete a book by ISBN.
     */
    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String isbn) {
        if (!repo.existsById(isbn)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + isbn);
        }
        repo.deleteById(isbn);
    }
}
