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
     * Optional query params:
     *   sortBy  = price | title | author | inventory
     *   order   = asc | desc
     *   genre   = exact genre match (case insensitive)
     *   minPrice, maxPrice = numeric filter on price
     */
    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false, defaultValue = "asc") String order,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice
    ) {
        // sorting
        Sort sort = Sort.unsorted();

        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction direction =
                    "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;

            String property = switch (sortBy.toLowerCase()) {
                case "title" -> "title";
                case "price" -> "price";
                case "inventory" -> "inventory";
                case "author" -> "author";
                default -> null; // unknown sort field ignored
            };

            if (property != null) {
                sort = Sort.by(direction, property);
            }
        }

        List<Book> books = sort.isUnsorted()
                ? repo.findAll()
                : repo.findAll(sort);

        // filtering in memory
        return books.stream()
                .filter(b ->
                        genre == null || genre.isBlank()
                                || (b.getGenre() != null && b.getGenre().equalsIgnoreCase(genre)))
                .filter(b ->
                        minPrice == null || b.getPrice() >= minPrice)
                .filter(b ->
                        maxPrice == null || b.getPrice() <= maxPrice)
                .toList();
    }

    /**
     * GET /api/books/{isbn}
     */
    @GetMapping("/{isbn}")
    public Book getBookByIsbn(@PathVariable String isbn) {
        return repo.findById(isbn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + isbn));
    }

    /**
     * GET /api/books/search?query=...
     */
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String query) {
        return repo.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    /**
     * POST /api/books
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
     */
    @PutMapping("/{isbn}")
    public Book upsert(@PathVariable String isbn, @RequestBody Book book) {
        book.setIsbn(isbn);
        return repo.save(book);
    }

    /**
     * DELETE /api/books/{isbn}
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
