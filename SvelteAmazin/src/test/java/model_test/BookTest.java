package model_test;

import com.amazin.svelteamazin.model.Book;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    @Test
    void testNoArgConstructorAndSettersGetters() {
        Book book = new Book();
        book.setIsbn("978-0-123456-47-2");
        book.setTitle("Test Title");
        book.setAuthor("Author Name");
        book.setPublisher("Publisher Name");
        book.setDescription("Sample description");
        book.setPrice(19.99);
        book.setInventory(5);
        book.setImageUrl("http://example.com/image.jpg");

        assertEquals("978-0-123456-47-2", book.getIsbn());
        assertEquals("Test Title", book.getTitle());
        assertEquals("Author Name", book.getAuthor());
        assertEquals("Publisher Name", book.getPublisher());
        assertEquals("Sample description", book.getDescription());
        assertEquals(19.99, book.getPrice(), 1e-6);
        assertEquals(5, book.getInventory());
        assertEquals("http://example.com/image.jpg", book.getImageUrl());
    }

    @Test
    void testAllArgsConstructor() {
        Book book = new Book("978-1-234567-89-7", "Another Title", "A. Author", "Some Publisher",
                "Another description", 0.0, 0, "http://example.com/another.jpg");

        assertAll("book",
                () -> assertEquals("978-1-234567-89-7", book.getIsbn()),
                () -> assertEquals("Another Title", book.getTitle()),
                () -> assertEquals("A. Author", book.getAuthor()),
                () -> assertEquals("Some Publisher", book.getPublisher()),
                () -> assertEquals("Another description", book.getDescription()),
                () -> assertEquals(0.0, book.getPrice(), 1e-6),
                () -> assertEquals(0, book.getInventory()),
        () -> assertEquals("http://example.com/another.jpg", book.getImageUrl())
        );
    }

    @Test
    void testMutability() {
        Book book = new Book("isbn", "title", "author", "pub", "desc", 1.0, 10, "http://example.com/img.png");
        book.setPrice(2.5);
        book.setInventory(7);

        assertEquals(2.5, book.getPrice(), 1e-6);
        assertEquals(7, book.getInventory());
        assertEquals("http://example.com/img.png", book.getImageUrl());
    }
}

