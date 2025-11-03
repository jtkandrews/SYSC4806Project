package com.amazin.svelteamazin.controller;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class BookControllerTest {

    BookController bookController;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // Instantiate the controller
        bookController = new BookController(bookRepository);
        bookRepository.deleteAll();

        Book book1 = new Book("9780804139021", "The Martian", "Andy Weir", "Crown", "Astronaut stranded on Mars",
                19.99, 5, "https://covers.openlibrary.org/b/isbn/ 9780804139021-L.jpg");
        Book book2 = new Book("1234", "Test2", "Author", "pub", "genre",
                9.99, 3, "https://google.ca");
        bookRepository.save(book1);
        bookRepository.save(book2);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    void getAllBooks() {
        List<Book> result = bookController.getAllBooks(null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("9780804139021", result.get(0).getIsbn());
        assertEquals("1234", result.get(1).getIsbn());
    }

    @Test
    void getBookByIsbn() {
        Book result = bookController.getBookByIsbn("9780804139021");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                bookController.getBookByIsbn("-99"));

        assertNotNull(result);
        assertEquals("9780804139021", result.getIsbn());
        assertEquals("The Martian", result.getTitle());

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
//        Exception exceptionTrue = new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + "-99");
        assertEquals(HttpStatus.NOT_FOUND + " \"Book not found: " + "-99\"", exception.getMessage());
    }

    @Test
    void create() {
        List<Book> result = bookController.getAllBooks(null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        Book book2 = new Book("12343", "Test3", "Author3", "pub3", "genre3",
                9.993, 33, "https://google.ca3");
        bookRepository.save(book2);


        List<Book> result2 = bookController.getAllBooks(null, null);

        assertEquals(3, result2.size());
        assertEquals("12343", result2.get(2).getIsbn());
        assertEquals("Test3", result2.get(2).getTitle());
        assertEquals("Author3", result2.get(2).getAuthor());
        assertEquals("pub3", result2.get(2).getPublisher());
        assertEquals("genre3", result2.get(2).getDescription());
        assertEquals(9.993, result2.get(2).getPrice());
        assertEquals(33, result2.get(2).getInventory());
        assertEquals("https://google.ca3", result2.get(2).getImageUrl());
    }

    @Test
    void upsert() {
        Book book2 = new Book("12343", "Test3", "Author3", "pub3", "genre3",
                9.993, 33, "https://google.ca3");
        bookRepository.save(book2);

        Book result = bookController.getBookByIsbn("12343");
        assertEquals("12343", result.getIsbn());
        assertEquals("Test3", result.getTitle());
        assertEquals("Author3", result.getAuthor());
        assertEquals("pub3", result.getPublisher());
        assertEquals("genre3", result.getDescription());
        assertEquals(9.993, result.getPrice());
        assertEquals(33, result.getInventory());
        assertEquals("https://google.ca3", result.getImageUrl());

        Book result2 = bookController.upsert("1234", book2);

        assertEquals("1234", result2.getIsbn());
        assertEquals("Test3", result2.getTitle());
        assertEquals("Author3", result2.getAuthor());
        assertEquals("pub3", result2.getPublisher());
        assertEquals("genre3", result2.getDescription());
        assertEquals(9.993, result2.getPrice());
        assertEquals(33, result2.getInventory());
        assertEquals("https://google.ca3", result2.getImageUrl());
    }

    @Test
    void delete() {
        List<Book> result = bookController.getAllBooks(null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());


        Book bookToDelete = result.get(0); // Assuming you want to delete the first book
        bookController.delete(bookToDelete.getIsbn());

        List<Book> updatedResult = bookController.getAllBooks(null, null);

        // Assert the record is deleted
        assertEquals(1, updatedResult.size());
        assertFalse(updatedResult.stream().anyMatch(b -> b.getIsbn().equals(bookToDelete.getIsbn())));
    }

    @Test
    void testSearchBooksCaseInsensitiveAndLiveList() {

        Book b1 = new Book("1", "Java Programming", "Alice Example", "Pub", "Desc", 10.0, 1, "www.1.com");
        Book b2 = new Book("2", "Advanced Java", "Bob Example", "Pub", "Desc", 15.0, 2, "www.2.com");
        Book b3 = new Book("3", "Python Guide", "Carol Example", "Pub", "Desc", 12.0, 3, "www.3.com");

        bookRepository.save(b1);
        bookRepository.save(b2);
        bookRepository.save(b3);

        // case-insensitive title search
        List<Book> javaResults = bookController.searchBooks("java");
        assertEquals(2, javaResults.size(), "should find both Java titles ignoring case");

        // author search
        List<Book> aliceResults = bookController.searchBooks("alice");
        assertEquals(1, aliceResults.size());
        assertEquals("Java Programming", aliceResults.get(0).getTitle());

        // verify the returned list is affected by mutating the live list
        bookRepository.delete(b1);
        List<Book> javaAfterRemove = bookController.searchBooks("java");
        assertEquals(1, javaAfterRemove.size(), "removing from the live list should change search results");
    }
}