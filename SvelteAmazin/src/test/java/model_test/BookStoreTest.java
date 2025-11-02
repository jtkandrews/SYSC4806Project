package model_test;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookStoreTest {

    @Test
    void testInitiallyEmpty() {
        BookStore store = new BookStore();
        assertTrue(store.getAllBooks().isEmpty(), "new store should start empty when not managed by Spring");
    }

    @Test
    void testGetBookByIsbnNotFound() {
        BookStore store = new BookStore();
        Optional<Book> result = store.getBookByIsbn("nonexistent");
        assertTrue(result.isEmpty(), "searching for unknown ISBN should return empty Optional");
    }

    @Test
    void testAddAndGetBookByIsbn() {
        BookStore store = new BookStore();
        Book b = new Book("978-0-111111-11-1", "Test Title", "Author Name", "Publisher", "Desc", 19.99, 5, "http://example.com/test.jpg");
        store.getAllBooks().add(b);

        Optional<Book> found = store.getBookByIsbn("978-0-111111-11-1");
        assertTrue(found.isPresent(), "book added to the live list should be findable by ISBN");

        Book got = found.get();
        assertAll("fields",
                () -> assertEquals("Test Title", got.getTitle()),
                () -> assertEquals("Author Name", got.getAuthor()),
                () -> assertEquals("Publisher", got.getPublisher()),
                () -> assertEquals(19.99, got.getPrice(), 1e-6),
                () -> assertEquals(5, got.getInventory()),
                () -> assertEquals("http://example.com/test.jpg", got.getImageUrl())
        );
    }

    @Test
    void testSearchBooksCaseInsensitiveAndLiveList() {
        BookStore store = new BookStore();

        Book b1 = new Book("1", "Java Programming", "Alice Example", "Pub", "Desc", 10.0, 1, "img1.jpg");
        Book b2 = new Book("2", "Advanced Java", "Bob Example", "Pub", "Desc", 15.0, 2, "img2.jpg");
        Book b3 = new Book("3", "Python Guide", "Carol Example", "Pub", "Desc", 12.0, 3, "img3.jpg");

        store.getAllBooks().addAll(List.of(b1, b2, b3));

        // case-insensitive title search
        List<Book> javaResults = store.searchBooks("java");
        assertEquals(2, javaResults.size(), "should find both Java titles ignoring case");

        // author search
        List<Book> aliceResults = store.searchBooks("alice");
        assertEquals(1, aliceResults.size());
        assertEquals("Java Programming", aliceResults.get(0).getTitle());
        assertEquals("img1.jpg", aliceResults.get(0).getImageUrl());

        // verify the returned list is affected by mutating the live list
        store.getAllBooks().remove(b1);
        List<Book> javaAfterRemove = store.searchBooks("java");
        assertEquals(1, javaAfterRemove.size(), "removing from the live list should change search results");
    }
}
