package com.cyber.client.model;

public class Food {
    private int id;
    private String name;
    private String description;
    private String imgSrc;
    private double price;
    private String color;

    public Food(int id, String name, String description, String imgSrc, double price, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imgSrc = imgSrc;
        this.price = price;
        this.color = color;
    }

    public Food() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
