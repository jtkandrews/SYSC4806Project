package com.amazin.svelteamazin.model;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BookStore {

    private final List<Book> books = new ArrayList<>();

    @PostConstruct
    public void loadBooksFromFile() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("books.csv"))))) {

            books.addAll(reader.lines()
                    .skip(1)
                    .map(this::parseBook)
                    .collect(Collectors.toList()));

            System.out.println("Loaded " + books.size() + " books from file.");
        } catch (Exception e) {
            System.err.println("⚠️ Error loading books file: " + e.getMessage());
        }
    }

    private Book parseBook(String line) {
        String[] data = line.split(",");
        return new Book(
                data[0], data[1], data[2], data[3], data[4],
                Double.parseDouble(data[5]), Integer.parseInt(data[6])
        );
    }

    public List<Book> getAllBooks() {
        return books;
    }

    public Optional<Book> getBookByIsbn(String isbn) {
        return books.stream().filter(b -> b.getIsbn().equals(isbn)).findFirst();
    }

    public List<Book> searchBooks(String keyword) {
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || b.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}
