package com.amazin.svelteamazin;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.io.*;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class SvelteAmazinApplication {

    public static void main(String[] args) {
        SpringApplication.run(SvelteAmazinApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(BookRepository bookRepository) {

        return (args) -> {
            String fileName = "books.csv";

//            int i = 0;

            // ✅ Recommended best practice: load as InputStream so it works in JAR and IDE
            try (InputStream input = Book.class.getClassLoader().getResourceAsStream(fileName)) {
                if (input == null) {
                    throw new IllegalArgumentException("File not found: " + fileName);
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                    reader.readLine();// Skip separator line
                    reader.readLine();// Skip header line

                    // Read the rest line by line
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Trim and skip empty lines
                        if (line.isBlank()) continue;

                        // Split on commas
                        String[] parts = line.split("\\|", -1); // -1 keeps empty strings

                        // Expecting 8 columns
                        if (parts.length < 8) {
                            System.err.println("Skipping invalid line: " + line);
                            continue;
                        }

                        String isbn = parts[0].trim();
                        String title = parts[1].trim();
                        String author = parts[2].trim();
                        String publisher = parts[3].trim();
                        String genre = parts[4].trim();
                        double price = Double.parseDouble(parts[5].trim());
                        int inventory = Integer.parseInt(parts[6].trim());
                        String imageUrl = parts[7].trim();

                        bookRepository.save(new Book(isbn, title, author, publisher, genre, price, inventory, imageUrl));
//                        System.out.println(i);
//                        i++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Books loaded");


//            // Create some initial books
//            Book book1 = new Book("9780804139021", "The Martian", "Andy Weir", "Crown", "Astronaut stranded on Mars",
//                    19.99, 5, "https://covers.openlibrary.org/b/isbn/ 9780804139021-L.jpg");

            // Save the books in the repository
//            bookRepository.save(book1);
        };
    }
}
