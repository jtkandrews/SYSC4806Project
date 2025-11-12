package com.amazin.svelteamazin;

import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList; // Added for the custom split
import java.util.List; // Added for the custom split

@SpringBootApplication
public class SvelteAmazinApplication {

    public static void main(String[] args) {
        SpringApplication.run(SvelteAmazinApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(BookRepository bookRepository) {

        return (args) -> {
            String fileName = "books.csv";

            // ✅ Recommended best practice: load as InputStream so it works in JAR and IDE
            try (InputStream input = Book.class.getClassLoader().getResourceAsStream(fileName)) {
                if (input == null) {
                    throw new IllegalArgumentException("File not found: " + fileName);
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                    reader.readLine();// Skip separator line (if present)
                    reader.readLine();// Skip header line

                    // Read the rest line by line
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Trim and skip empty lines
                        if (line.isBlank()) continue;

                        // --- Custom Quotes-Aware CSV Split ---
                        // Replaces the brittle line.split(",", -1)
                        List<String> partList = new ArrayList<>();
                        boolean inQuotes = false;
                        StringBuilder currentPart = new StringBuilder();

                        for (char c : line.toCharArray()) {
                            if (c == '"') {
                                inQuotes = !inQuotes;
                            } else if (c == ',' && !inQuotes) {
                                // Found an outside comma: this is a delimiter
                                partList.add(currentPart.toString());
                                currentPart.setLength(0); // Reset the buffer
                                continue;
                            }
                            currentPart.append(c);
                        }
                        // Add the last part
                        partList.add(currentPart.toString());

                        String[] parts = partList.toArray(new String[0]);
                        // -------------------------------------

                        // Expecting 9 columns (The correct number)
                        if (parts.length < 9) {
                            System.err.println("Skipping invalid line (too few columns): " + line);
                            continue;
                        }

                        // Parse the fields
                        String isbn = parts[0].trim();
                        // Strip quotes from Title and Publisher if present
                        String title = parts[1].trim().replaceAll("^\"|\"$", "");
                        String author = parts[2].trim();
                        String publisher = parts[3].trim().replaceAll("^\"|\"$", "");
                        String description = parts[4].trim(); // New column
                        String genre = parts[5].trim();
                        double price = Double.parseDouble(parts[6].trim());
                        int inventory = Integer.parseInt(parts[7].trim());
                        String imageUrl = parts[8].trim();

                        // Save the Book
                        bookRepository.save(new Book(isbn, title, author, publisher, genre, description,price, inventory, imageUrl));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Books loaded");
        };
    }
}