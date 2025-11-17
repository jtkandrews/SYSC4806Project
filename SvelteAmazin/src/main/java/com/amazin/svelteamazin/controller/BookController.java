package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import org.springframework.data.domain.Sort;
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
     * Return all books, optionally sorted by a field and order.
     * Example: /api/books?sortBy=price&order=desc
     */
    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false, defaultValue = "asc") String order
    ) {
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;

            // Ensure only allowed fields are sortable
            return switch (sortBy.toLowerCase()) {
                case "title" -> repo.findAll(Sort.by(direction, "title"));
                case "price" -> repo.findAll(Sort.by(direction, "price"));
                case "inventory" -> repo.findAll(Sort.by(direction, "inventory"));
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field: " + sortBy);
            };
        }

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
     * GET /api/books/filter
     * Filter books by genre and/or price range.
     * Example: /api/books/filter?genre=Fiction&minPrice=10&maxPrice=30
     */
    @GetMapping("/filter")
    public List<Book> filterBooks(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        List<Book> books = repo.findAll();

        if (genre != null && !genre.isBlank()) {
            books = books.stream()
                    .filter(b -> b.getGenre() != null && b.getGenre().equalsIgnoreCase(genre))
                    .toList();
        }

        if (minPrice != null) {
            books = books.stream()
                    .filter(b -> b.getPrice() >= minPrice)
                    .toList();
        }

        if (maxPrice != null) {
            books = books.stream()
                    .filter(b -> b.getPrice() <= maxPrice)
                    .toList();
        }

        return books;
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
