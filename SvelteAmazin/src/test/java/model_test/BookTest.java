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
        book.setGenre("Fiction");
        book.setDescription("Sample description");
        book.setPrice(19.99);
        book.setInventory(5);
        book.setImageUrl("http://example.com/image.jpg");

        assertEquals("978-0-123456-47-2", book.getIsbn());
        assertEquals("Test Title", book.getTitle());
        assertEquals("Author Name", book.getAuthor());
        assertEquals("Publisher Name", book.getPublisher());
        assertEquals("Fiction", book.getGenre());
        assertEquals("Sample description", book.getDescription());
        assertEquals(19.99, book.getPrice(), 1e-6);
        assertEquals(5, book.getInventory());
        assertEquals("http://example.com/image.jpg", book.getImageUrl());
    }

    @Test
    void testAllArgsConstructor() {
        Book book = new Book("978-1-234567-89-7", "Another Title", "A. Author", "Some Publisher",
                "Genre", "Another description", 29.99, 5, "http://example.com/another.jpg");

        assertAll("book",
                () -> assertEquals("978-1-234567-89-7", book.getIsbn()),
                () -> assertEquals("Another Title", book.getTitle()),
                () -> assertEquals("A. Author", book.getAuthor()),
                () -> assertEquals("Some Publisher", book.getPublisher()),
                () -> assertEquals("Genre", book.getGenre()),
                () -> assertEquals("Another description", book.getDescription()),
                () -> assertEquals(29.99, book.getPrice(), 1e-6),
                () -> assertEquals(5, book.getInventory()),
                () -> assertEquals("http://example.com/another.jpg", book.getImageUrl())
        );
    }

    @Test
    void testMutability() {
        Book book = new Book("isbn", "title", "author", "pub", "genre", "desc", 1.0, 10, "http://example.com/img.png");
        book.setPrice(2.5);
        book.setInventory(7);

        assertEquals(2.5, book.getPrice(), 1e-6);
        assertEquals(7, book.getInventory());
        assertEquals("http://example.com/img.png", book.getImageUrl());
    }

    @Test
    void testMultipleGenresAsCommaSeparatedString() {
        // Test that genre field can store multiple genres as comma-separated values
        Book book = new Book();
        book.setIsbn("978-0-123456-47-2");
        book.setTitle("Multi-Genre Book");
        book.setAuthor("Author Name");
        book.setPublisher("Publisher Name");
        book.setGenre("Fiction, Mystery, Thriller");
        book.setDescription("A book with multiple genres");
        book.setPrice(24.99);
        book.setInventory(10);
        book.setImageUrl("http://example.com/image.jpg");

        assertEquals("Fiction, Mystery, Thriller", book.getGenre());
        assertTrue(book.getGenre().contains("Fiction"));
        assertTrue(book.getGenre().contains("Mystery"));
        assertTrue(book.getGenre().contains("Thriller"));
    }

    @Test
    void testSingleGenre() {
        // Test that genre field works with single genre
        Book book = new Book("isbn123", "Single Genre Book", "Author", "Publisher",
                "Science Fiction", "A sci-fi book", 19.99, 5, "http://example.com/img.jpg");

        assertEquals("Science Fiction", book.getGenre());
        assertFalse(book.getGenre().contains(","));
    }

    @Test
    void testNullGenre() {
        // Test that genre field can be null (optional field)
        Book book = new Book();
        book.setIsbn("978-0-123456-47-2");
        book.setTitle("No Genre Book");
        book.setAuthor("Author Name");
        book.setPublisher("Publisher Name");
        book.setGenre(null);
        book.setDescription("A book without genre");
        book.setPrice(15.99);
        book.setInventory(3);
        book.setImageUrl("http://example.com/image.jpg");

        assertNull(book.getGenre());
    }

    @Test
    void testEmptyGenre() {
        // Test that genre field can be empty string
        Book book = new Book("isbn456", "Empty Genre Book", "Author", "Publisher",
                "", "A book with empty genre", 12.99, 7, "http://example.com/img.jpg");

        assertEquals("", book.getGenre());
        assertNotNull(book.getGenre());
    }

    @Test
    void testLongGenreString() {
        // Test that genre field can handle long comma-separated list
        String longGenreList = "Fiction, Non-Fiction, Science Fiction, Fantasy, Mystery, Thriller, Romance, Horror, Biography, History";
        Book book = new Book();
        book.setGenre(longGenreList);

        assertEquals(longGenreList, book.getGenre());
        assertTrue(book.getGenre().split(", ").length >= 10);
    }
}

