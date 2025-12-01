package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import com.amazin.svelteamazin.model.Order;
import com.amazin.svelteamazin.repository.OrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookRepository repo;
    private final OrderRepository orderRepository;

    public BookController(BookRepository repo, OrderRepository orderRepository) {
        this.repo = repo;
        this.orderRepository = orderRepository;
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

    @GetMapping("/recommended_books")
    public List<Book> getAllRecBooks() {

        List<Order> orders = orderRepository.findAll();
        List<Book> allBooks = repo.findAll();

        // === CASE 1: No orders at all → just return 8 random books ===
        if (orders.isEmpty()) {
            Collections.shuffle(allBooks);
            return allBooks.stream().limit(8).toList();
        }

        // Extract purchased ISBN lists (by order)
        List<List<String>> booksIsbns = new ArrayList<>();
        Set<String> allPurchasedIsbns = new HashSet<>(); // used for fallback later
        for (Order order : orders) {
            List<String> isbns = order.getBookIsbns();
            booksIsbns.add(isbns);
            allPurchasedIsbns.addAll(isbns);
        }

        // === Step 1: Compute similarity (Jaccard) ===
        Map<String, Double> similarityScores = new HashMap<>();

        for (int i = 0; i < booksIsbns.size(); i++) {
            for (int j = i + 1; j < booksIsbns.size(); j++) {
                String[] a = booksIsbns.get(i).toArray(new String[0]);
                String[] b = booksIsbns.get(j).toArray(new String[0]);

                double sim = jaccardSimilarity(a, b);
                if (sim > 0) {
                    similarityScores.put(i + "," + j, sim);
                    System.out.println("Similarity score: " + sim);
                }
            }
        }

        // === Step 2: Build recommended set from intersections ===
        Set<String> recommendedIsbns = intersectionBooks(booksIsbns, similarityScores);

        // === Convert ISBNs → Books ===
        List<Book> recommendedBooks = new ArrayList<>();
        for (String isbn : recommendedIsbns) {
            repo.findById(isbn).ifPresent(recommendedBooks::add);
        }


        // If less than 8: add random previously purchased books
        int needed = 8 - recommendedBooks.size();
        if (needed > 0) {
            List<Book> previouslyPurchased = new ArrayList<>();

            for (Book b : allBooks) {
                if (allPurchasedIsbns.contains(b.getIsbn()) &&
                        !recommendedIsbns.contains(b.getIsbn())) {
                    previouslyPurchased.add(b);
                }
            }

            Collections.shuffle(previouslyPurchased);

            for (int i = 0; i < needed && i < previouslyPurchased.size(); i++) {
                recommendedBooks.add(previouslyPurchased.get(i));
                recommendedIsbns.add(previouslyPurchased.get(i).getIsbn());
            }

            needed = 8 - recommendedBooks.size();
        }

        // If still less than 8: add random books NEVER purchased
        if (needed > 0) {
            List<Book> neverPurchasedBooks = new ArrayList<>();

            for (Book b : allBooks) {
                if (!allPurchasedIsbns.contains(b.getIsbn()) &&
                        !recommendedIsbns.contains(b.getIsbn())) {

                    neverPurchasedBooks.add(b);
                }
            }

            Collections.shuffle(neverPurchasedBooks);

            for (int i = 0; i < needed && i < neverPurchasedBooks.size(); i++) {
                recommendedBooks.add(neverPurchasedBooks.get(i));
            }
        }

        return recommendedBooks;
    }

    //Calculate jaccard similarity based on two arrays of type Long
    static private  double jaccardSimilarity(String[] a, String[] b) {
        Set<String> s1 = new LinkedHashSet<>(Arrays.asList(a));
        Set<String> s2 = new LinkedHashSet<>(Arrays.asList(b));

        Set<String> intersection = new LinkedHashSet<>(s1);
        intersection.retainAll(s2);

        Set<String> union = new LinkedHashSet<>(s1);
        union.addAll(s2);

        double jaccardSimilarity = (double)intersection.size()/ (double)union.size();
//        System.out.println(intersection + " " + union);
        return jaccardSimilarity;
    }

    //Output any books that are in both lists
    //First digit of key is the first purchase id books and second is the second purchased id books
    static private Set<String> intersectionBooks(List<List<String>> booksIsbns, Map<String, Double> similarityScores) {
        Set<String> recommendedIsbns = new LinkedHashSet<>();

        for (String key : similarityScores.keySet()) {
            String[] pair = key.split(",");
            int i = Integer.parseInt(pair[0]);
            int j = Integer.parseInt(pair[1]);

            Set<String> s1 = new HashSet<>(booksIsbns.get(i));
            Set<String> s2 = new HashSet<>(booksIsbns.get(j));

            s1.retainAll(s2);
            recommendedIsbns.addAll(s1);
        }
        return recommendedIsbns;
    }

}