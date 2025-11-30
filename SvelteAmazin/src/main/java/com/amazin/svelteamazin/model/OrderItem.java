package com.amazin.svelteamazin.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    private String isbn;
    private String title;
    private double price;
    private int quantity;
    private String imageUrl;

    public OrderItem() {}

    public OrderItem(Order order, String isbn, String title, double price, int quantity, String imageUrl) {
        this.order = order;
        this.isbn = isbn;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}