package com.amazin.svelteamazin.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id @Column(length = 20)
    private String isbn;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String author;
    private String publisher;
    @Column(length = 4000) private String genre;
    @Column(nullable = false) private double price;
    @Column(nullable = false) private int inventory;
    private String imageUrl;


    public Book() {}

    public Book(String isbn, String title, String author, String publisher,
                String genre, double price, int inventory, String imageUrl) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.genre = genre;
        this.price = price;
        this.inventory = inventory;
        this.imageUrl = imageUrl;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public String getDescription() { return genre; }
    public double getPrice() { return price; }
    public int getInventory() { return inventory; }
    public String getImageUrl() { return imageUrl; }


    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setDescription(String genre) { this.genre = genre; }
    public void setPrice(double price) { this.price = price; }
    public void setInventory(int inventory) { this.inventory = inventory; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
