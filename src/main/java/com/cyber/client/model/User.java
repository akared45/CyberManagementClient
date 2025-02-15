package com.cyber.client.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String name;
    private String password;
    private double balance;
    private LocalDateTime created;

    public User() {
    }

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public User(int id, String name, String password, double balance, LocalDateTime created) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.balance = balance;
        this.created = created;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
