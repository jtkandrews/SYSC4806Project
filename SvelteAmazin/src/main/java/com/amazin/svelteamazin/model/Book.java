package com.amazin.svelteamazin.model;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String description;
    private double price;
    private int inventory;
    private String imageUrl;


    public Book() {}

    public Book(String isbn, String title, String author, String publisher,
                String description, double price, int inventory, String imageUrl) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.price = price;
        this.inventory = inventory;
        this.imageUrl = imageUrl;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getInventory() { return inventory; }
    public String getImageUrl() { return imageUrl; }


    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setInventory(int inventory) { this.inventory = inventory; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
