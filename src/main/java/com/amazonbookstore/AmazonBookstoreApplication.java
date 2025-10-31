package com.amazonbookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Verified backend setup - Anas

@SpringBootApplication
public class AmazonBookstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmazonBookstoreApplication.class, args);
        System.out.println(" Amazon Bookstore backend is running on http://localhost:8080");
    }
}
